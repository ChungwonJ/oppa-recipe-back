package org.example.domain.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.global.auth.jwt.entity.RefreshToken;
import org.example.global.auth.jwt.repository.RefreshTokenRepository;
import org.example.global.auth.jwt.provider.JwtTokenProvider;
import org.example.global.base.ApiResponse;
import org.example.global.common.cookie.CookieUtil;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refreshToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없거나 만료되었습니다.");
        }

        String email = tokenProvider.getEmail(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "로그아웃된 사용자입니다."));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 요청입니다.");
        }

        String newAccessToken = tokenProvider.createAccessToken(email);
        return ApiResponse.of(Collections.singletonMap("accessToken", newAccessToken));
    }

    @Transactional
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response, String errorMessage) {
        cookieUtil.deleteCookie(request, response, "refreshToken");

        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            try {
                String email = tokenProvider.getEmail(refreshToken);
                refreshTokenRepository.deleteByEmail(email);
            } catch (Exception e) {
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
            }
        }

        return ApiResponse.of("로그아웃 되었습니다.");
    }

    /**
     * 쿠키에서 리프레시 토큰을 추출하는 공통 로직
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}