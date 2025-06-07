package kr.jaehwan.auth.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String role
) {
    public WithOutRefreshToken withOutRefreshToken() {
        return new WithOutRefreshToken(this.accessToken, this.role);
    }

    public record WithOutRefreshToken(
            String accessToken,
            String role
    ) {
    }
}
