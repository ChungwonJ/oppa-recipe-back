package org.example.domain.user.controller;

import jakarta.servlet.http.Cookie;
import org.example.global.auth.jwt.entity.RefreshToken;
import org.example.global.auth.jwt.provider.JwtTokenProvider;
import org.example.global.auth.jwt.repository.RefreshTokenRepository;
import org.example.global.common.cookie.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰 재발급 성공")
    void refreshToken_Success() throws Exception {
        // given
        String refreshToken = "valid-refresh-token";
        String email = "test@naver.com";
        String newAccessToken = "new-access-token";
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        given(tokenProvider.validateToken(refreshToken)).willReturn(true);
        given(tokenProvider.getEmail(refreshToken)).willReturn(email);

        RefreshToken savedToken = new RefreshToken(email, refreshToken);
        given(refreshTokenRepository.findByEmail(email)).willReturn(Optional.of(savedToken));
        given(tokenProvider.createAccessToken(email)).willReturn(newAccessToken);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andDo(print());
    }

    @Test
    @DisplayName("DB의 토큰과 일치하지 않으면 401 에러 발생")
    void refreshToken_Mismatch_Fail() throws Exception {
        // given
        String requestToken = "mismatch-token";
        String dbToken = "stored-token";
        Cookie cookie = new Cookie("refreshToken", requestToken);

        given(tokenProvider.validateToken(requestToken)).willReturn(true);
        given(tokenProvider.getEmail(requestToken)).willReturn("test@naver.com");

        RefreshToken savedToken = new RefreshToken("test@naver.com", dbToken);
        given(refreshTokenRepository.findByEmail(anyString())).willReturn(Optional.of(savedToken));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 요청입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 시 쿠키와 DB 토큰 삭제 성공")
    void logout_Success() throws Exception {
        // given
        String refreshToken = "valid-token";
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        given(tokenProvider.getEmail(refreshToken)).willReturn("test@naver.com");

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("로그아웃 되었습니다."))
                .andDo(print());

        verify(cookieUtil).deleteCookie(any(), any(), eq("refreshToken"));
        verify(refreshTokenRepository).deleteByEmail("test@naver.com");
    }

    @Test
    @DisplayName("로그아웃 도중 DB 에러 발생 시 500 에러 반환")
    void logout_ServerError_Fail() throws Exception {
        // given
        String refreshToken = "valid-token";
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        given(tokenProvider.getEmail(refreshToken)).willReturn("test@naver.com");
        // DB 삭제 시 예외 발생 시뮬레이션
        doThrow(new RuntimeException("DB Error")).when(refreshTokenRepository).deleteByEmail(anyString());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(cookie)
                        .param("errorMessage", "로그아웃 처리 중 서버 오류가 발생했습니다."))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("로그아웃 처리 중 서버 오류가 발생했습니다."))
                .andDo(print());
    }
}