package org.example.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration apiConfig = new CorsConfiguration();

        apiConfig.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://oppa-recipe.vercel.app"
        ));

        apiConfig.setAllowCredentials(true);

        apiConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        apiConfig.setAllowedHeaders(List.of("*"));
        apiConfig.setExposedHeaders(List.of("Authorization"));

        source.registerCorsConfiguration("/**", apiConfig);
        return new CorsFilter(source);
    }
}