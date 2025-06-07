package kr.jaehwan.auth.domain.auth.provider;

import kr.jaehwan.auth.domain.auth.dto.OAuthUserInfo;

public interface OAuthProvider {
    OAuthUserInfo fetchUserInfo(String accessToken);
    String providerName();

}
