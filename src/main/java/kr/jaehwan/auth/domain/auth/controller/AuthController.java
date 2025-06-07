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
import kr.jaehwan.auth.domain.auth.service.AuthService;
import kr.jaehwan.auth.global.dto.ResponseDto;
import kr.jaehwan.auth.global.util.ApiUtil;
import kr.jaehwan.auth.global.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value="/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<LoginResponse.WithOutRefreshToken>> register(
            @Valid @RequestBody final LocalRegisterRequest localRegisterRequest,
            final HttpServletResponse httpServletResponse
    ) {
        LoginResponse loginResponse = authService.registerLocal(localRegisterRequest.email(), localRegisterRequest.password(), localRegisterRequest.name());
        return handleLogin(loginResponse, httpServletResponse, "로컬 회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponse.WithOutRefreshToken>> login(
            @Valid @RequestBody final LocalLoginRequest localLoginRequest,
            final HttpServletResponse httpServletResponse
    ) {
        LoginResponse loginResponse = authService.loginLocal(localLoginRequest.email(), localLoginRequest.password());
        return handleLogin(loginResponse, httpServletResponse, "로컬 로그인 성공");
    }

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<ResponseDto<LoginResponse.WithOutRefreshToken>> loginWithOAuth(
            @PathVariable String provider,
            @Valid @RequestBody final OAuthLoginRequest oauthLoginRequest,
            final HttpServletResponse httpServletResponse
    ) {
        LoginResponse loginResponse = authService.loginWithOAuth(provider, oauthLoginRequest.token());
        return handleLogin(loginResponse, httpServletResponse, provider + " 로그인 성공");
    }

    @GetMapping("/reissue")
    public ResponseEntity<ResponseDto<AccessToken>> reissue(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse
    ) {
        String refreshToken = CookieUtil.extract(httpServletRequest, "refreshToken");
        TokenResponse tokenResponse = authService.reissue(refreshToken);

        httpServletResponse.addCookie(CookieUtil.makeCookie("refreshToken", tokenResponse.refreshToken()));
        ResponseDto<AccessToken> responseDto = ApiUtil.success(200, "재발급 성공", new AccessToken(tokenResponse.accessToken()));
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(final HttpServletRequest httpServletRequest) {
        String refreshToken = CookieUtil.extract(httpServletRequest, "refreshToken");
        authService.logout(refreshToken);

        ResponseDto<Void> responseDto = ApiUtil.success(200, "로그아웃 성공", null);
        return ResponseEntity.ok(responseDto);
    }

    private ResponseEntity<ResponseDto<LoginResponse.WithOutRefreshToken>> handleLogin(
            LoginResponse loginResponse,
            HttpServletResponse httpServletResponse,
            String message
    ) {
        httpServletResponse.addCookie(CookieUtil.makeCookie("refreshToken", loginResponse.refreshToken()));
        var body = loginResponse.withOutRefreshToken();
        return ResponseEntity.ok(ApiUtil.success(200, message, body));
    }
}
