package org.example.global.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.global.enums.UserRole;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAccessAspect {

    @Before("@annotation(org.example.global.annotation.Admin)")
    public void checkAdminAccess() {
        checkRole(UserRole.ROLE_ADMIN, "관리자 권한이 필요합니다.");
    }

    @Before("@annotation(org.example.global.annotation.Member)")
    public void checkMemberAccess() {
        checkRole(UserRole.ROLE_USER, "사용자 권한이 필요합니다.");
    }

    private void checkRole(UserRole requiredRole, String errorMessage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. 로그인 여부 확인
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다.");
        }

        // 2. 권한 확인 (SecurityContext에 담긴 GrantedAuthority 이용)
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(requiredRole.getUserRole()));

        if (!hasRole) {
            throw new CustomException(HttpStatus.FORBIDDEN, errorMessage);
        }
    }
}
