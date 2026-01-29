package org.example.global.config;

import lombok.RequiredArgsConstructor;
import org.example.global.auth.jwt.filter.JwtAuthenticationFilter;
import org.example.global.auth.jwt.provider.JwtTokenProvider;
import org.example.global.auth.oauth.CustomOAuth2UserService;
import org.example.global.auth.oauth.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenProvider tokenProvider; // 추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OAuth2 관련 엔드포인트들을 확실히 열어줘야 합니다.
                        .requestMatchers("/", "/api/auth/**", "/login/**", "/oauth2/**","/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // 1. baseUri를 "/api/auth/login" 정도로 짧게 잡으세요.
                        // 이렇게 하면 실제 접속 주소는 /api/auth/login/naver 가 됩니다.
                        .authorizationEndpoint(e -> e.baseUri("/api/auth/login"))

                        // 2. redrectionEndpoint는 가급적 yml의 redirect-uri와 일치시켜야 하며,
                        // 기본값인 /login/oauth2/code/naver를 쓰는 것이 가장 에러가 적습니다.
                        // 커스텀을 꼭 해야 한다면 yml 설정과 100% 일치하는지 확인하세요.
                        .redirectionEndpoint(r -> r.baseUri("/api/auth/naver/callback"))

                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                // ... (이하 logout, exceptionHandling 동일)
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
