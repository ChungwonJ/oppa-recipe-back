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

    private User getValidatedUserByNaverId(String naverId) {
        return userRepository.findByNaverIdAndIsDeletedFalse(naverId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public UserResponse getMyInfo(String naverId) {
        User user = getValidatedUserByNaverId(naverId);
        return UserResponse.from(user);
    }

    @Transactional
    public void updateMyInfo(String naverId, UserUpdateRequest request) {
        User user = getValidatedUserByNaverId(naverId);
        user.updateInfo(request.getName(), request.getPhoneNumber());
    }

    @Transactional
    public void deleteMe(String naverId) {
        User user = getValidatedUserByNaverId(naverId);
        user.softDelete();
    }
}