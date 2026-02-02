package org.example.domain.youtube.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.gemini.service.FoodVisionService;
import org.example.domain.youtube.service.YoutubeService;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final YoutubeService youtubeService;

    @GetMapping("/shorts")
    public ResponseEntity<String> getFoodShorts(@RequestParam String foodName) {
        String shortsUrl = youtubeService.getMostLikedShorts(foodName);

        if (shortsUrl == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "관련 쇼츠 영상을 찾을 수 없습니다.");
        }
        
        return ResponseEntity.ok(shortsUrl);
    }
}
