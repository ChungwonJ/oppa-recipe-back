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
        // 1. 쿠키에서 토큰 추출
        String refreshToken = getRefreshTokenFromCookie(request);

        // 2. 토큰 유효성 검증 (실패 시 CustomException 던지기 -> 핸들러가 처리)
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없거나 만료되었습니다.");
        }

        // 3. DB 일치 여부 확인
        String email = tokenProvider.getEmail(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "로그아웃된 사용자입니다."));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 요청입니다.");
        }

        // 4. 새로운 액세스 토큰 발급 및 규격 응답
        String newAccessToken = tokenProvider.createAccessToken(email);
        return ApiResponse.of(Collections.singletonMap("accessToken", newAccessToken));
    }

    @Transactional
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키 삭제
        cookieUtil.deleteCookie(request, response, "refreshToken");

        // 2. DB 토큰 삭제
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            try {
                String email = tokenProvider.getEmail(refreshToken);
                refreshTokenRepository.deleteByEmail(email);
            } catch (Exception e) {
                // 로그아웃 과정 중 DB 삭제 실패는 ServerException으로 던지거나 로그만 남김
                // 여기서는 흐름을 위해 예외를 던지지 않고 무사히 완료 처리
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