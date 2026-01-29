package org.example.global.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.global.auth.jwt.entity.RefreshToken;
import org.example.global.auth.jwt.repository.RefreshTokenRepository;
import org.example.global.auth.jwt.provider.JwtTokenProvider;
import org.example.global.exception.ServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
        String email = (String) responseMap.get("email");

        String accessToken = tokenProvider.createAccessToken(email);
        String refreshToken = tokenProvider.createRefreshToken(email);

        // DB 저장/업데이트
        try {
            refreshTokenRepository.findByEmail(email)
                    .ifPresentOrElse(
                            token -> token.updateToken(refreshToken),
                            () -> refreshTokenRepository.save(new RefreshToken(email, refreshToken))
                    );
        } catch (Exception e) {
            // 소셜 로그인 성공 후 DB 작업 실패 시
            throw new ServerException("로그인 처리 중 서버 오류가 발생했습니다.");
        }

        // Refresh Token 쿠키 설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(true)
                .secure(true) // 로컬 테스트 시 false로 하면 더 편할 수 있음 (HTTPS 아닐 때)
                .maxAge(refreshTokenExpiration / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // 테스트를 위해 리다이렉트 주소를 백엔드 주소나 프론트 주소로 확인하세요
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/api/auth/naver/callback")
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
