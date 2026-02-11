package org.example.domain.youtube.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.youtube.dto.response.RecipeAnalysisResponse;
import org.example.domain.youtube.dto.response.YoutubeSearchResultResponse;
import org.example.domain.youtube.service.YoutubeService;
import org.example.global.base.ApiResponse;
import org.example.global.exception.CustomException;
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
    public ResponseEntity<ApiResponse<RecipeAnalysisResponse>> getShortsRecipe(
            @RequestParam(name = "foodName") String foodName) {

        YoutubeSearchResultResponse searchResult = youtubeService.getMostLikedShorts(foodName);

        if (searchResult == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, foodName + "에 대한 적절한 레시피 영상을 찾을 수 없습니다.");
        }

        RecipeAnalysisResponse response = youtubeService.analyzeRecipe(searchResult, foodName);

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
