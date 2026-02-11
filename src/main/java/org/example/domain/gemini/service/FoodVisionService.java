package org.example.domain.gemini.service;

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
public class FoodVisionService {

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    @Value("${google.gemini.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractFoodName(MultipartFile file) throws IOException {

        String fullUrl = geminiUrl + geminiApiKey;

        if (file == null || file.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "업로드된 파일이 비어 있습니다.");
        }

        String fileName = (file.getOriginalFilename() != null) ? file.getOriginalFilename().toLowerCase() : "";
        String contentType = file.getContentType();

        boolean isImage = (contentType != null && contentType.startsWith("image/")) ||
                fileName.endsWith(".jfif") || fileName.endsWith(".jpg") ||
                fileName.endsWith(".jpeg") || fileName.endsWith(".png") ||
                fileName.endsWith(".webp");

        if (!isImage) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다. (인식된 타입: " + contentType + ")");
        }

        // 이미지를 Base64 문자열로 변환
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        String mimeType;

        if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.endsWith(".webp")) {
            mimeType = "image/webp";
        } else if (fileName.endsWith(".heic") || fileName.endsWith(".heif")) {
            mimeType = "image/heic";
        } else {
            mimeType = "image/jpeg";
        }

        // Gemini API 규격에 맞는 JSON 바디 구성
        String prompt = "이 사진 속의 음식을 분석해서 한국어로 메뉴 이름만 딱 하나만 대답해줘. " +
                "예: '김치볶음밥', '쌀국수'. " +
                "만약 사진에 음식이 없거나 무엇인지 판단할 수 없다면 반드시 '음식이아님'이라고만 대답해줘.";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64Image
                                ))
                        )
                ))
        );

        try {
            // API 호출 및 응답
            Map<String, Object> response = restTemplate.postForObject(fullUrl, requestBody, Map.class);

            // 응답 JSON에서 텍스트 데이터만 추출
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            String result = parts.get(0).get("text").toString().trim();

            if (result.contains("음식이아님") || result.length() > 20) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "사진에서 음식을 찾을 수 없습니다. 음식 사진을 업로드해주세요.");
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.BAD_REQUEST, "분석 실패: " + e.getMessage());
        }
    }
}