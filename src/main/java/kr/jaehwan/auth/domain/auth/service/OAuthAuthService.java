package kr.jaehwan.auth.domain.auth.service;

import jakarta.transaction.Transactional;
import kr.jaehwan.auth.domain.auth.dto.OAuthUserInfo;
import kr.jaehwan.auth.domain.auth.dto.response.LoginResponse;
import kr.jaehwan.auth.domain.auth.provider.OAuthProvider;
import kr.jaehwan.auth.domain.user.entity.User;
import kr.jaehwan.auth.domain.user.entity.UserAuth;
import kr.jaehwan.auth.domain.user.entity.type.AuthType;
import kr.jaehwan.auth.domain.user.repository.UserAuthRepository;
import kr.jaehwan.auth.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthAuthService {

    private final Map<String, OAuthProvider> oauthProviders;
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final TokenService tokenService;

    @Transactional
    public LoginResponse loginWithOAuth(String providerName, String accessToken) {
        OAuthProvider oauthProvider = Optional.ofNullable(
                        oauthProviders.get(providerName.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + providerName));

        OAuthUserInfo oauthUserInfo = oauthProvider.fetchUserInfo(accessToken);

        // LOCAL 계정 중복 방지
        userRepository.findByEmail(oauthUserInfo.getEmail())
                .flatMap(userAuthRepository::findByUser)
                .filter(userAuth -> userAuth.getAuthType() == AuthType.LOCAL)
                .ifPresent(userAuth -> {
                    throw new IllegalStateException("Email registered locally: " + oauthUserInfo.getEmail());
                });

        // 이미 매핑된 OAuth ID인가?
        UserAuth mappedUserAuth = userAuthRepository.findByAuthTypeAndOauthProviderId(
                        AuthType.valueOf(oauthUserInfo.getProvider()),
                        oauthUserInfo.getProviderId())
                .orElse(null);

        User user = (mappedUserAuth != null)
                ? mappedUserAuth.getUser()
                : createGuestAndMapping(oauthUserInfo, providerName);

        return tokenService.buildLoginResponse(user);
    }

    private User createGuestAndMapping(OAuthUserInfo oauthUserInfo, String providerName) {
        User guestUser = userRepository.save(
                User.builder()
                        .email(oauthUserInfo.getEmail())
                        .name(oauthUserInfo.getName())
                        .imageUrl(oauthUserInfo.getImageUrl())
                        .build()
        );

        userAuthRepository.save(
                UserAuth.builder()
                        .authType(AuthType.valueOf(oauthUserInfo.getProvider()))
                        .oauthProvider(providerName)
                        .oauthProviderId(oauthUserInfo.getProviderId())
                        .user(guestUser)
                        .build()
        );
        return guestUser;
    }
}
