package org.example.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.user.dto.request.UserUpdateRequest;
import org.example.domain.user.dto.response.UserResponse;
import org.example.domain.user.service.UserService;
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

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        return ResponseEntity.ok(userService.getUser(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody UserUpdateRequest request) {
        if (userDetails == null) throw new CustomException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");

        userService.updateUser(id, userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) throw new CustomException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");

        userService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
