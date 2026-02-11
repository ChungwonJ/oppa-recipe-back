package org.example.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.user.dto.response.UserResponse;
import org.example.domain.user.dto.request.UserUpdateRequest;
import org.example.domain.user.entity.User;
import org.example.domain.user.repository.UserRepository;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    // 공통 검증 메서드
    private User getValidatedUser(Long id, String naverId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!user.getNaverId().equals(naverId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "해당 리소스에 대한 권한이 없습니다.");
        }
        return user;
    }

    public UserResponse getUser(Long id, String naverId) {
        User user = getValidatedUser(id, naverId);
        return UserResponse.from(user);
    }

    @Transactional
    public void updateUser(Long id, String naverId, UserUpdateRequest request) {
        User user = getValidatedUser(id, naverId);
        user.updateInfo(request.getName(), request.getPhoneNumber());
    }

    @Transactional
    public void deleteUser(Long id, String naverId) {
        User user = getValidatedUser(id, naverId);
        user.softDelete();
    }
}