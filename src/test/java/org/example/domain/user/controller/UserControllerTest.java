package org.example.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.user.dto.request.UserUpdateRequest;
import org.example.domain.user.dto.response.UserResponse;
import org.example.domain.user.service.UserService;
import org.example.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    private final String BASE_URL = "/api/v1/users";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private SecurityMockMvcRequestPostProcessors.OAuth2LoginRequestPostProcessor mockNaver(String email) {
        Map<String, Object> response = new HashMap<>();
        response.put("email", email);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", response);

        return oauth2Login().oauth2User(new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "response"
        ));
    }

    @Test
    @DisplayName("본인의 ID로 상세 정보 조회 시 성공(200)")
    void getUser_Success() throws Exception {
        Long userId = 1L;
        String email = "owner@naver.com";
        UserResponse response = UserResponse.builder().email(email).name("주인공").build();

        given(userService.getUser(eq(userId), eq(email))).willReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", userId)
                        .with(mockNaver(email))) // 헬퍼 사용
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("다른 사용자의 ID로 수정 시도 시 403 Forbidden 에러 발생")
    void updateUser_Forbidden_Fail() throws Exception {
        Long otherUserId = 999L;
        String myEmail = "attacker@naver.com";
        UserUpdateRequest request = new UserUpdateRequest("해킹이름", "010-0000-0000");

        doThrow(new CustomException(HttpStatus.FORBIDDEN, "해당 리소스에 대한 권한이 없습니다."))
                .when(userService).updateUser(eq(otherUserId), eq(myEmail), any());

        mockMvc.perform(put(BASE_URL + "/{id}", otherUserId)
                        .with(mockNaver(myEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 리소스에 대한 권한이 없습니다."));
    }

    @Test
    @DisplayName("본인 계정 탈퇴 요청 시 성공(204)")
    void deleteUser_Success() throws Exception {
        Long myId = 1L;
        String myEmail = "user@naver.com";

        mockMvc.perform(delete(BASE_URL + "/{id}", myId)
                        .with(mockNaver(myEmail))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(myId, myEmail);
    }
}