package kr.jaehwan.auth.domain.auth.service;

import jakarta.transaction.Transactional;
import kr.jaehwan.auth.domain.auth.dto.response.LoginResponse;
import kr.jaehwan.auth.domain.auth.dto.response.TokenResponse;
import kr.jaehwan.auth.domain.auth.entity.RefreshToken;
import kr.jaehwan.auth.domain.auth.exception.NotFoundRefreshTokenException;
import kr.jaehwan.auth.domain.auth.repository.RefreshTokenRepository;
import kr.jaehwan.auth.domain.user.entity.User;
import kr.jaehwan.auth.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse buildLoginResponse(User user) {
        String accessToken  = jwtProvider.createAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), user.getRole());
        return new LoginResponse(accessToken, refreshToken, user.getRole().name());
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        RefreshToken storedRefreshToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(NotFoundRefreshTokenException::getInstance);

        TokenResponse tokenResponse = jwtProvider.createTokenResponse(
                storedRefreshToken.getEmail(),
                storedRefreshToken.getUserRole()
        );

        refreshTokenRepository.delete(storedRefreshToken);
        return tokenResponse;
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
    }
}

