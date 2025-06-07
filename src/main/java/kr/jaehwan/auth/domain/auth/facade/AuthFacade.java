package kr.jaehwan.auth.domain.auth.facade;

import kr.jaehwan.auth.domain.auth.dto.response.LoginResponse;
import kr.jaehwan.auth.domain.auth.dto.response.TokenResponse;
import kr.jaehwan.auth.domain.auth.service.LocalAuthService;
import kr.jaehwan.auth.domain.auth.service.OAuthAuthService;
import kr.jaehwan.auth.domain.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final LocalAuthService localAuthService;
    private final OAuthAuthService oAuthAuthService;
    private final TokenService tokenService;

    public LoginResponse registerLocal(String email, String password, String name) {
        return localAuthService.registerLocal(email, password, name);
    }

    public LoginResponse loginLocal(String email, String password) {
        return localAuthService.loginLocal(email, password);
    }

    public LoginResponse loginWithOAuth(String providerName, String accessToken) {
        return oAuthAuthService.loginWithOAuth(providerName, accessToken);
    }

    public TokenResponse reissue(String refreshToken) {
        return tokenService.reissue(refreshToken);
    }

    public void logout(String refreshToken) {
        tokenService.logout(refreshToken);
    }
}
