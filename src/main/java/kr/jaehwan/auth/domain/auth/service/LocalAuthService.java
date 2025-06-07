package kr.jaehwan.auth.domain.auth.service;

import jakarta.transaction.Transactional;
import kr.jaehwan.auth.domain.auth.dto.response.LoginResponse;
import kr.jaehwan.auth.domain.user.entity.User;
import kr.jaehwan.auth.domain.user.entity.UserAuth;
import kr.jaehwan.auth.domain.user.entity.type.AuthType;
import kr.jaehwan.auth.domain.user.repository.UserAuthRepository;
import kr.jaehwan.auth.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public LoginResponse registerLocal(String email, String rawPassword, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already in use: " + email);
        }

        User newUser = userRepository.save(
                User.builder()
                        .email(email)
                        .name(name)
                        .build()
        );

        userAuthRepository.save(
                UserAuth.builder()
                        .authType(AuthType.LOCAL)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .user(newUser)
                        .build()
        );

        return tokenService.buildLoginResponse(newUser);
    }

    @Transactional
    public LoginResponse loginLocal(String email, String rawPassword) {
        UserAuth localUserAuth = userAuthRepository.findByUserEmail(email)
                .filter(auth -> auth.getAuthType() == AuthType.LOCAL)
                .orElseThrow(() -> new IllegalArgumentException("No local account: " + email));

        if (!passwordEncoder.matches(rawPassword, localUserAuth.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return tokenService.buildLoginResponse(localUserAuth.getUser());
    }
}