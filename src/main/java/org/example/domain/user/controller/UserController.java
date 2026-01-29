package org.example.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.user.dto.response.UserResponse;
import org.example.domain.user.dto.request.UserUpdateRequest;
import org.example.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> attributes = (Map<String, Object>) principal.getAttributes().get("response");
        String email = (String) attributes.get("email");

        return ResponseEntity.ok(userService.getMyInfo(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMe(@AuthenticationPrincipal OAuth2User principal,
                                         @RequestBody UserUpdateRequest request) {
        String email = extractEmail(principal);
        userService.updateMyProfile(email, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal OAuth2User principal) {
        userService.deleteUser(extractEmail(principal));
        return ResponseEntity.noContent().build();
    }

    private String extractEmail(OAuth2User principal) {
        Map<String, Object> response = (Map<String, Object>) principal.getAttributes().get("response");
        return (String) response.get("email");
    }
}
