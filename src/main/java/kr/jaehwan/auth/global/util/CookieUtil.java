package kr.jaehwan.auth.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static Cookie makeCookie(final String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(1800);
        cookie.setHttpOnly(true);
        return cookie;
    }

    public static String extract(final HttpServletRequest httpServletRequest, final String cookieName) {
        if(httpServletRequest == null) return null;

        for(Cookie cookie : httpServletRequest.getCookies()) {
            if(cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
