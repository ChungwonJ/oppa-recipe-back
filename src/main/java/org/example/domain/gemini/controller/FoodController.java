package org.example.domain.gemini.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.gemini.service.FoodVisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodVisionService foodVisionService;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeFood(@RequestParam("file") MultipartFile file) {
        try {
            String result = foodVisionService.extractFoodName(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("분석 실패: " + e.getMessage());
        }
    }
}