package kr.jaehwan.auth.domain.auth.service;

import jakarta.transaction.Transactional;
import kr.jaehwan.auth.domain.auth.dto.OAuthUserInfo;
import kr.jaehwan.auth.domain.auth.dto.response.LoginResponse;
import kr.jaehwan.auth.domain.auth.dto.response.TokenResponse;
import kr.jaehwan.auth.domain.auth.entity.RefreshToken;
import kr.jaehwan.auth.domain.auth.exception.NotFoundRefreshTokenException;
import kr.jaehwan.auth.domain.auth.provider.OAuthProvider;
import kr.jaehwan.auth.domain.auth.repository.RefreshTokenRepository;
import kr.jaehwan.auth.domain.user.entity.User;
import kr.jaehwan.auth.domain.user.entity.UserAuth;
import kr.jaehwan.auth.domain.user.entity.type.AuthType;
import kr.jaehwan.auth.domain.user.entity.type.Role;
import kr.jaehwan.auth.domain.user.repository.UserAuthRepository;
import kr.jaehwan.auth.domain.user.repository.UserRepository;
import kr.jaehwan.auth.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final Map<String, OAuthProvider> oauthProviders;
    private final UserRepository             userRepository;
    private final UserAuthRepository         userAuthRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder            passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResponse registerLocal(String email, String rawPassword, String name) {
        checkEmailNotUsed(email);
        User user = createUser(email, name);
        createLocalAuth(user, rawPassword);
        return buildLoginResponse(user);
    }

    @Transactional
    public LoginResponse loginLocal(String email, String rawPassword) {
        UserAuth auth = userAuthRepository
                .findByUserEmail(email)
                .filter(a -> a.getAuthType() == AuthType.LOCAL)
                .orElseThrow(() -> new IllegalArgumentException("No local account: " + email));

        if (!passwordEncoder.matches(rawPassword, auth.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return buildLoginResponse(auth.getUser());
    }

    @Transactional
    public LoginResponse loginWithOAuth(String providerName, String accessToken) {
        OAuthProvider oauthProvider = findProvider(providerName);
        OAuthUserInfo oauthUserInfo = oauthProvider.fetchUserInfo(accessToken);

        blockIfLocalRegistered(oauthUserInfo.getEmail());
        User user = findOrCreateOAuthUser(oauthUserInfo);
        return buildLoginResponse(user);
    }

    @Transactional
    public TokenResponse reissue(final String token) {
        RefreshToken refreshToken = refreshTokenRepository.findById(token)
                .orElseThrow(NotFoundRefreshTokenException::getInstance);

        String email = refreshToken.getEmail();
        Role role = refreshToken.getUserRole();
        TokenResponse tokenResponse = jwtProvider.createTokenResponse(email, role);

        refreshTokenRepository.delete(refreshToken);
        return tokenResponse;
    }

    @Transactional
    public void logout(final String token) {
        refreshTokenRepository.deleteById(token);
    }

    /*---------------------- helpers ----------------------*/

    private void checkEmailNotUsed(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already in use: " + email);
        }
    }

    private void blockIfLocalRegistered(String email) {
        userRepository.findByEmail(email)
                .flatMap(userAuthRepository::findByUser)
                .filter(userAuth -> userAuth.getAuthType() == AuthType.LOCAL)
                .ifPresent(userAuth -> {
                    throw new IllegalStateException("Email registered locally: " + email);
                });
    }

    private OAuthProvider findProvider(String name) {
        return Optional.ofNullable(oauthProviders.get(name.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + name));
    }

    private User createUser(String email, String name) {
        User u = User.builder()
                .email(email)
                .name(name)
                .role(Role.GUEST)
                .active(true)
                .deleted(false)
                .build();
        return userRepository.save(u);
    }

    private void createLocalAuth(User user, String rawPassword) {
        String hash = passwordEncoder.encode(rawPassword);
        UserAuth userAuth = UserAuth.builder()
                .authType(AuthType.LOCAL)
                .passwordHash(hash)
                .user(user)
                .build();
        userAuthRepository.save(userAuth);
    }

    private User findOrCreateOAuthUser(OAuthUserInfo oauthUserInfo) {
        return userAuthRepository
                .findByAuthTypeAndOauthProviderId(
                        AuthType.valueOf(oauthUserInfo.getProvider()),
                        oauthUserInfo.getProviderId()
                )
                .map(UserAuth::getUser)
                .orElseGet(() -> {
                    User user = createUser(oauthUserInfo.getEmail(), oauthUserInfo.getName());
                    UserAuth userAuth = UserAuth.builder()
                            .authType(AuthType.valueOf(oauthUserInfo.getProvider()))
                            .oauthProvider(oauthUserInfo.getProvider())
                            .oauthProviderId(oauthUserInfo.getProviderId())
                            .user(user)
                            .build();
                    userAuthRepository.save(userAuth);
                    return user;
                });
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), user.getRole());
        return new LoginResponse(accessToken, refreshToken, user.getRole().name());
    }
}
