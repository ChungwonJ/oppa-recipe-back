package org.example.global.common.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .sameSite("None") // 크로스 도메인 설정 시 필요
                .secure(true)     // HTTPS 필수
                .httpOnly(true)   // JS 접근 차단
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                            .path("/")
                            .httpOnly(true)
                            .secure(true)
                            .maxAge(0) // 즉시 만료
                            .build();
                    response.addHeader("Set-Cookie", deleteCookie.toString());
                }
            }
        }
    }
}
