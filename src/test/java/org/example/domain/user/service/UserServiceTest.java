package org.example.domain.user.service;

import org.example.domain.user.dto.request.UserUpdateRequest;
import org.example.domain.user.dto.response.UserResponse;
import org.example.domain.user.entity.User;
import org.example.domain.user.repository.UserRepository;
import org.example.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("사용자 조회 성공: ID와 Email이 일치할 때")
    void getUser_Success() {
        // given
        Long userId = 1L;
        String email = "test@naver.com";
        User user = User.builder()
                .email(email)
                .name("테스터")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse result = userService.getUser(userId, email);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("사용자 조회 실패: 해당 ID의 사용자가 없을 때 (404)")
    void getUser_NotFound_Fail() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(userId, "any@email.com"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 조회 실패: ID는 존재하지만 이메일이 다를 때 (403)")
    void getUser_Forbidden_Fail() {
        // given
        Long userId = 1L;
        String ownerEmail = "owner@naver.com";
        String attackerEmail = "attacker@naver.com";

        User user = User.builder()
                .email(ownerEmail)
                .name("실제주인")
                .build();

        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // when & then
        assertThatThrownBy(() -> userService.getUser(userId, attackerEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN)
                .hasMessageContaining("해당 리소스에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUser_Success() {
        // given
        Long userId = 1L;
        String email = "test@naver.com";
        User user = User.builder()
                .email(email)
                .name("이전이름")
                .build();

        UserUpdateRequest request = new UserUpdateRequest("새이름", "010-1111-2222");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.updateUser(userId, email, request);

        // then
        assertThat(user.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("사용자 삭제 시 실제 삭제 대신 Soft Delete가 수행된다")
    void deleteUser_SoftDelete_Success() {
        // given
        Long userId = 1L;
        String email = "test@naver.com";
        User user = User.builder().email(email).build();
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.deleteUser(userId, email);

        // then
        assertThat(user.getIsDeleted()).isTrue();
        verify(userRepository, never()).delete(any());
    }
}