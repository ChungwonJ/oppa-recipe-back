package org.example.domain.youtube.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.youtube.dto.response.YoutubeSearchResultResponse;
import org.example.domain.youtube.service.YoutubeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final YoutubeService youtubeService;

    @GetMapping("/recipe")
    public ResponseEntity<Map<String, String>> getShortsRecipe(@RequestParam String foodName) {
        // 1. 유튜브 인기 쇼츠 검색 (URL + Title)
        YoutubeSearchResultResponse searchResult = youtubeService.getMostLikedShorts(foodName);

        if (searchResult == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "영상을 찾을 수 없습니다."));
        }

        // 2. 제목 정보를 포함하여 Gemini 분석 요청
        String recipeAnalysis = youtubeService.analyzeRecipe(searchResult);

        // 3. 결과 응답
        Map<String, String> response = new HashMap<>();
        response.put("foodName", foodName);
        response.put("videoTitle", searchResult.getTitle());
        response.put("shortsUrl", searchResult.getUrl());
        response.put("recipe", recipeAnalysis);

        return ResponseEntity.ok(response);
    }
}
