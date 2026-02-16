package org.example.domain.gemini.client;

import lombok.RequiredArgsConstructor;
import org.example.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Value("${google.gemini.url}")
    private String url;

    private final RestTemplate restTemplate;

    // 프롬프트는 상수로 관리하여 유지보수성 향상
    private static final String FOOD_PROMPT = "이 사진 속의 음식을 분석해서 한국어로 메뉴 이름만 딱 하나만 대답해줘. " +
            "예: '김치볶음밥', '쌀국수'. " +
            "만약 사진에 음식이 없거나 무엇인지 판단할 수 없다면 반드시 '음식이아님'이라고만 대답해줘.";

    public String getAnalysis(String base64Image, String mimeType) {
        String fullUrl = url + apiKey;
        Map<String, Object> requestBody = createRequestBody(base64Image, mimeType);

        try {
            Map<String, Object> response = restTemplate.postForObject(fullUrl, requestBody, Map.class);
            return parseResponse(response);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Gemini API 호출 중 서버 오류가 발생했습니다.");
        }
    }

    private Map<String, Object> createRequestBody(String base64Image, String mimeType) {
        return Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", FOOD_PROMPT),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64Image
                                ))
                        )
                ))
        );
    }

    private String parseResponse(Map<String, Object> response) {
        try {
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "음식이아님";

            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");
            return parts.get(0).get("text").toString().trim();
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini 응답 형식이 변경되었습니다.");
        }
    }
}