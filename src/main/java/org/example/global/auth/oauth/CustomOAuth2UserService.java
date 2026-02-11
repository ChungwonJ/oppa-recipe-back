package org.example.global.auth.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.domain.user.entity.User;
import org.example.domain.user.repository.UserRepository;
import org.example.global.enums.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String naverId = (String) response.get("id");
        String email = (String) response.get("email");
        String name = (String) response.get("name");
        String mobile = (String) response.get("mobile");
        System.out.println("네이버에서 넘어온 이메일: " + email);
        try {
            User user = userRepository.findByNaverIdAndIsDeletedFalse(naverId)
                    .map(entity -> {
                        entity.updateInfo(name, mobile);
                        return entity;
                    })
                    .orElseGet(() -> userRepository.save(User.builder()
                            .naverId(naverId)
                            .email(email)
                            .name(name)
                            .phoneNumber(mobile)
                            .role(UserRole.ROLE_USER)
                            .build()));

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(user.getRole().getUserRole())),
                    attributes,
                    "response"
            );
        } catch (Exception e) {
            // DB 통신 장애
            throw new OAuth2AuthenticationException("사용자 정보를 저장하는 도중 오류가 발생했습니다.");
        }
    }
}