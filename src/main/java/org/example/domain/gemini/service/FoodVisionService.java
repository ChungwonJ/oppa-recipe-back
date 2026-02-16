package org.example.domain.gemini.service;

import lombok.RequiredArgsConstructor;
import org.example.global.common.client.GeminiClient;
import org.example.global.common.processor.ImageProcessor;
import org.example.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FoodVisionService {

    private final GeminiClient geminiClient;
    private final ImageProcessor imageProcessor;

    public String extractFoodName(MultipartFile file) {
        imageProcessor.validate(file);

        String base64Image = imageProcessor.encodeToBase64(file);
        String mimeType = imageProcessor.getMimeType(file);

        String result = geminiClient.getAnalysis(base64Image, mimeType);

        if ("음식이아님".equals(result) || result.length() > 20) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "음식 사진이 아니거나 인식할 수 없습니다.");
        }

        return result;
    }
}