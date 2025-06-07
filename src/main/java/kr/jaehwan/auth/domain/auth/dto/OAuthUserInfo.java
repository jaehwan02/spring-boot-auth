package kr.jaehwan.auth.domain.auth.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfo {
    private final String provider;
    private final String providerId;
    private final String email;
    private final String name;
    private final String imageUrl;
}