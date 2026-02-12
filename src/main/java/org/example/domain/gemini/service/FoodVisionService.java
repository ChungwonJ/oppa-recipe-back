package org.example.domain.gemini.service;

import lombok.RequiredArgsConstructor;
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

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    @Value("${google.gemini.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate;

    public String extractFoodName(MultipartFile file) {
        validateImageFile(file);

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = determineMimeType(file);
            String fullUrl = geminiUrl + geminiApiKey;

            // 요청 바디 구성
            Map<String, Object> requestBody = createGeminiRequestBody(base64Image, mimeType);

            // API 호출
            Map<String, Object> response = restTemplate.postForObject(fullUrl, requestBody, Map.class);

            // 데이터 추출 로직
            String result = parseGeminiResponse(response);

            if (result.equals("음식이아님") || result.length() > 20) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "사진에서 음식을 찾을 수 없습니다.");
            }

            return result;

        } catch (IOException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 읽기 실패");
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Gemini 분석 중 오류 발생: " + e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "업로드된 파일이 비어 있습니다.");
        }

        String contentType = file.getContentType();
        String fileName = (file.getOriginalFilename() != null) ? file.getOriginalFilename().toLowerCase() : "";

        boolean isJpgFamily = fileName.endsWith(".jfif") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
        boolean isImageMime = contentType != null && contentType.startsWith("image/");

        if (!isImageMime && !isJpgFamily) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다. (현재 형식: " + contentType + ")");
        }
    }

    private String determineMimeType(MultipartFile file) {
        String fileName = (file.getOriginalFilename() != null) ? file.getOriginalFilename().toLowerCase() : "";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".webp")) return "image/webp";
        if (fileName.endsWith(".heic") || fileName.endsWith(".heif")) return "image/heic";

        String contentType = file.getContentType();
        if (contentType != null && contentType.contains("jpeg")) return "image/jpeg";

        return "image/jpeg";
    }

    private Map<String, Object> createGeminiRequestBody(String base64Image, String mimeType) {
        String prompt = "이 사진 속의 음식을 분석해서 한국어로 메뉴 이름만 딱 하나만 대답해줘. " +
                "예: '김치볶음밥', '쌀국수'. " +
                "만약 사진에 음식이 없거나 무엇인지 판단할 수 없다면 반드시 '음식이아님'이라고만 대답해줘.";

        return Map.of(
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
    }

    @SuppressWarnings("unchecked")
    private String parseGeminiResponse(Map<String, Object> response) {
        try {
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");
            return parts.get(0).get("text").toString().trim();
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini 응답 파싱 실패");
        }
    }
}