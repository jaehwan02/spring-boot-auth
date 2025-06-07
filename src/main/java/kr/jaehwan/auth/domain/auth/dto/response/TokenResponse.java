package kr.jaehwan.auth.domain.auth.dto.response;

public record TokenResponse (
        String accessToken,
        String refreshToken
) {
}