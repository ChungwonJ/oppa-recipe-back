package org.example.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.user.dto.request.UserUpdateRequest;
import org.example.domain.user.dto.response.UserResponse;
import org.example.domain.user.service.UserService;
import org.example.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final String BASE_URL = "/api/v1/users";

    @Test
    @DisplayName("본인의 ID로 상세 정보 조회 시 성공(200)")
    void getUser_Success() throws Exception {
        // given
        Long userId = 1L;
        String email = "owner@naver.com";
        UserResponse response = UserResponse.builder()
                .email(email)
                .name("주인공")
                .build();

        given(userService.getUser(userId, email)).willReturn(response);

        // when & then
        mockMvc.perform(get(BASE_URL + "/{id}", userId)
                        .with(oauth2Login().attributes(attrs -> {
                            Map<String, Object> res = new HashMap<>();
                            res.put("email", email);
                            attrs.put("response", res);
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andDo(print());
    }

    @Test
    @DisplayName("다른 사용자의 ID로 수정 시도 시 403 Forbidden 에러 발생")
    void updateUser_Forbidden_Fail() throws Exception {
        // given
        Long otherUserId = 999L; // 내가 아닌 다른 사람의 ID
        String myEmail = "attacker@naver.com";
        UserUpdateRequest request = new UserUpdateRequest("해킹이름", "010-0000-0000");

        // 서비스 레이어에서 권한 부족 예외를 던지도록 설정
        doThrow(new CustomException(HttpStatus.FORBIDDEN, "해당 리소스에 대한 권한이 없습니다."))
                .when(userService).updateUser(eq(otherUserId), eq(myEmail), any());

        // when & then
        mockMvc.perform(put(BASE_URL + "/{id}", otherUserId)
                        .with(oauth2Login().attributes(attrs -> {
                            Map<String, Object> res = new HashMap<>();
                            res.put("email", myEmail);
                            attrs.put("response", res);
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()) // 403 응답 확인
                .andExpect(jsonPath("$.message").value("해당 리소스에 대한 권한이 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("본인 계정 탈퇴 요청 시 성공(204)")
    void deleteUser_Success() throws Exception {
        // given
        Long myId = 1L;
        String myEmail = "user@naver.com";

        // when & then
        mockMvc.perform(delete(BASE_URL + "/{id}", myId)
                        .with(oauth2Login().attributes(attrs -> {
                            Map<String, Object> res = new HashMap<>();
                            res.put("email", myEmail);
                            attrs.put("response", res);
                        })))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(myId, myEmail);
    }
}