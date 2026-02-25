package org.example.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.user.dto.request.UserUpdateRequest;
import org.example.domain.user.dto.response.UserResponse;
import org.example.domain.user.service.UserService;
import org.example.global.base.ApiResponse;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateAuthentication(userDetails);

        UserResponse response = userService.getUser(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PostMapping("/my")
    public ResponseEntity<ApiResponse<Long>> updateUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest request) {

        validateAuthentication(userDetails);

        userService.updateUser(id, userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.of(id));
    }

    @DeleteMapping("/my")
    public ResponseEntity<ApiResponse<Long>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateAuthentication(userDetails);

        userService.deleteUser(id, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(id));
    }

    private void validateAuthentication(UserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
    }
}
