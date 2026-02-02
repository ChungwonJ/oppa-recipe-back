package org.example.domain.gemini.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.gemini.service.FoodVisionService;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
            throw new CustomException(HttpStatus.BAD_REQUEST , e.getMessage());
        }
    }
}