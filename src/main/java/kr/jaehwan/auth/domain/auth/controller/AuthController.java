package kr.jaehwan.auth.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.jaehwan.auth.domain.auth.dto.request.LocalLoginRequest;
import kr.jaehwan.auth.domain.auth.dto.request.LocalRegisterRequest;
import kr.jaehwan.auth.domain.auth.dto.request.OAuthLoginRequest;
import kr.jaehwan.auth.domain.auth.dto.response.AccessToken;
import kr.jaehwan.auth.domain.auth.dto.response.LoginResponse;
import kr.jaehwan.auth.domain.auth.dto.response.TokenResponse;
import kr.jaehwan.auth.domain.auth.facade.AuthFacade;
import kr.jaehwan.auth.global.dto.ResponseDto;
import kr.jaehwan.auth.global.util.ApiUtil;
import kr.jaehwan.auth.global.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<LoginResponse.WithOutRefreshToken>> register(
            @Valid @RequestBody LocalRegisterRequest localRegisterRequest,
            HttpServletResponse httpServletResponse
    ) {
        LoginResponse loginResponse = authFacade.registerLocal(
                localRegisterRequest.email(),
                localRegisterRequest.password(),
                localRegisterRequest.name()
        );
        return buildResponse(
                loginResponse.withOutRefreshToken(),
                "로컬 회원가입 성공",
                httpServletResponse,
                loginResponse.refreshToken()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponse.WithOutRefreshToken>> login(
            @Valid @RequestBody LocalLoginRequest localLoginRequest,
            HttpServletResponse httpServletResponse
    ) {
        LoginResponse loginResponse = authFacade.loginLocal(
                localLoginRequest.email(),
                localLoginRequest.password()
        );
        return buildResponse(
                loginResponse.withOutRefreshToken(),
                "로컬 로그인 성공",
                httpServletResponse,
                loginResponse.refreshToken()
        );
    }

    @PostMapping("/oauth/{providerName}")
    public ResponseEntity<ResponseDto<LoginResponse.WithOutRefreshToken>> loginWithOAuth(
            @PathVariable String providerName,
            @Valid @RequestBody OAuthLoginRequest oAuthLoginRequest,
            HttpServletResponse httpServletResponse
    ) {
        LoginResponse loginResponse = authFacade.loginWithOAuth(
                providerName,
                oAuthLoginRequest.token()
        );
        return buildResponse(
                loginResponse.withOutRefreshToken(),
                providerName + " 로그인 성공",
                httpServletResponse,
                loginResponse.refreshToken()
        );
    }

    @GetMapping("/reissue")
    public ResponseEntity<ResponseDto<AccessToken>> reissue(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String refreshToken = CookieUtil.extract(httpServletRequest, "refreshToken");
        TokenResponse tokenResponse = authFacade.reissue(refreshToken);

        return buildResponse(
                new AccessToken(tokenResponse.accessToken()),
                "재발급 성공",
                httpServletResponse,
                tokenResponse.refreshToken()
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String refreshToken = CookieUtil.extract(httpServletRequest, "refreshToken");
        authFacade.logout(refreshToken);

        return buildResponse(
                null,
                "로그아웃 성공",
                httpServletResponse,
                null
        );
    }

    private <T> ResponseEntity<ResponseDto<T>> buildResponse(
            T body,
            String message,
            HttpServletResponse httpServletResponse,
            String newRefreshToken
    ) {
        if (newRefreshToken != null) {
            httpServletResponse.addCookie(
                    CookieUtil.makeCookie("refreshToken", newRefreshToken)
            );
        }
        ResponseDto<T> responseDto = ApiUtil.success(200, message, body);
        return ResponseEntity.ok(responseDto);
    }
}
