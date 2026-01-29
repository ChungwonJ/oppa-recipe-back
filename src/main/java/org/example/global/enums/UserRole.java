package org.example.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ROLE_ADMIN(Authority.ADMIN),
    ROLE_USER(Authority.USER);

    private final String userRole;

    public static UserRole of(String userRole) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(userRole))
                .findFirst()
                .orElseThrow(() -> new CustomException(HttpStatus.FORBIDDEN, "유효하지 않은 권한입니다."));
    }

    public static class Authority {
        public static final String ADMIN = "ROLE_ADMIN";
        public static final String USER = "ROLE_USER";
    }
}