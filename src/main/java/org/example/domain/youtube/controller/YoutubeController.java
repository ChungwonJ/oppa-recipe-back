package org.example.domain.youtube.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.recipe.service.RecipeCrawlService;
import org.example.domain.recipe.service.RecipeService;
import org.example.domain.youtube.dto.response.RecipeAnalysisResponse;
import org.example.global.base.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final RecipeCrawlService recipeCrawlService;

    @GetMapping("/recipe")
    public ResponseEntity<ApiResponse<RecipeAnalysisResponse>> getShortsRecipe(
            @RequestParam String foodName) {
        return ResponseEntity.ok(ApiResponse.of(recipeCrawlService.collect(foodName)));
    }
}