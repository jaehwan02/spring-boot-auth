package kr.jaehwan.auth.global.feign;

import kr.jaehwan.auth.global.dto.GoogleUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "GoogleUserInfoFeignClient",
        url  = "https://www.googleapis.com/oauth2/v1/userinfo"
)
public interface GoogleUserInfoFeignClient {

    @GetMapping("?alt=json&access_token={token}")
    GoogleUserInfoResponse fetchUserInfo(
            @PathVariable("token") String token
    );
}