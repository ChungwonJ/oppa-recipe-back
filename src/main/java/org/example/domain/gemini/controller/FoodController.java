package org.example.domain.gemini.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.gemini.service.FoodVisionService;
import org.example.global.base.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> analyzeFood(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "분석할 이미지 파일이 없습니다.");
        }

        String foodName = foodVisionService.extractFoodName(file);

        return ResponseEntity.ok(ApiResponse.of(foodName));
    }
}