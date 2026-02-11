package org.example.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.global.base.BaseEntity;
import org.example.global.enums.UserRole;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String naverId;

    private String email;

    private String name;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder
    private User(String naverId, String email, String name, String phoneNumber, UserRole role) {
        this.naverId = naverId;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public void updateInfo(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}