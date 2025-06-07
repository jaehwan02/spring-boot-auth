package kr.jaehwan.auth.global.dto;

public record GoogleUserInfoResponse(
        String id,
        String email,
        String name,
        String imageUrl
) {
}
