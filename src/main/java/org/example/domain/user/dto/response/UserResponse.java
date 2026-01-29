package org.example.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.domain.user.entity.User;

@Getter
@Builder
public class UserResponse {
    private String email;
    private String name;
    private String phoneNumber;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
