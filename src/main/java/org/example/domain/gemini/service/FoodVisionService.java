package org.example.domain.gemini.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.gemini.client.GeminiClient;
import org.example.domain.gemini.util.ImageProcessor;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FoodVisionService {

    private final GeminiClient geminiClient;
    private final ImageProcessor imageProcessor;

    public String extractFoodName(MultipartFile file) {
        imageProcessor.validate(file);

        String base64Image = imageProcessor.encodeToBase64(file);
        String mimeType = imageProcessor.getMimeType(file);

        String result = geminiClient.analyzeFoodImage(base64Image, mimeType);

        if ("음식이아님".equals(result) || result.length() > 20) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "음식 사진이 아니거나 인식할 수 없습니다.");
        }

        return result;
    }
}