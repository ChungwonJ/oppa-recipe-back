package org.example.domain.youtube.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.recipe.service.RecipeCrawlService;
import org.example.domain.recipe.dto.response.RecipeAnalysisResponse;
import org.example.global.base.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
@Slf4j
public class YoutubeController {

    private final RecipeCrawlService recipeCrawlService;

    @GetMapping("/recipe")
    public ResponseEntity<ApiResponse<RecipeAnalysisResponse>> getShortsRecipe(
            @RequestParam String foodName) {

        long start = System.currentTimeMillis();
        RecipeAnalysisResponse response = recipeCrawlService.collect(foodName);
        log.info("=== [TOTAL] | foodName={} | total={}ms ===",
                foodName, System.currentTimeMillis() - start);

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}