package org.example.domain.youtube.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.youtube.dto.response.RecipeAnalysisResponse;
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
    public ResponseEntity<RecipeAnalysisResponse> getShortsRecipe(@RequestParam String foodName) {

        YoutubeSearchResultResponse searchResult = youtubeService.getMostLikedShorts(foodName);

        if (searchResult == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        RecipeAnalysisResponse response = youtubeService.analyzeRecipe(searchResult, foodName);

        return ResponseEntity.ok(response);
    }
}
