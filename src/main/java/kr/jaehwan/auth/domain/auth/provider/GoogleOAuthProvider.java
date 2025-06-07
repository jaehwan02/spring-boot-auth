package kr.jaehwan.auth.domain.auth.provider;

import kr.jaehwan.auth.domain.auth.dto.OAuthUserInfo;
import kr.jaehwan.auth.global.dto.GoogleUserInfoResponse;
import kr.jaehwan.auth.global.feign.GoogleUserInfoFeignClient;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuthProvider implements OAuthProvider {
    private final GoogleUserInfoFeignClient googleUserInfoFeignClient;

    public GoogleOAuthProvider(GoogleUserInfoFeignClient googleUserInfoFeignClient) {
        this.googleUserInfoFeignClient = googleUserInfoFeignClient;
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        GoogleUserInfoResponse googleUserInfoResponse = googleUserInfoFeignClient.fetchUserInfo(accessToken);
        return OAuthUserInfo.builder()
                .provider(providerName())
                .providerId(googleUserInfoResponse.id())
                .email(googleUserInfoResponse.email())
                .name(googleUserInfoResponse.name())
                .imageUrl(googleUserInfoResponse.imageUrl())
                .build();
    }

    @Override
    public String providerName() {
        return "GOOGLE";
    }
}
