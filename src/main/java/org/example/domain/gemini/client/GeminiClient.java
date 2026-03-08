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

    private static final String FOOD_PROMPT =
            "이 사진 속의 음식을 분석해서 한국어로 메뉴 이름만 딱 하나만 대답해줘. " +
                    "예: '김치볶음밥', '쌀국수'. " +
                    "만약 사진에 음식이 없거나 무엇인지 판단할 수 없다면 반드시 '음식이아님'이라고만 대답해줘.";

    private static final String RECIPE_PROMPT_TEMPLATE =
            "영상 제목: %s\n" +
                    "위 요리 영상의 내용을 분석해서 반드시 아래 JSON 형식으로만 답변해. 다른 설명은 하지마.\n\n" +
                    "{\n" +
                    "  \"recipe\": \"1. 조리법... 2. 조리법...\",\n" +
                    "  \"ingredients\": [\n" +
                    "    {\"name\": \"재료이름\", \"amount\": \"수량\"}\n" +
                    "  ]\n" +
                    "}\n\n" +
                    "조건:\n" +
                    "- 'name'은 쿠팡 검색용이므로 '당근', '계란'처럼 명사만 쓸 것.\n" +
                    "- 'amount': 그램수나 갯수 (예: '300g', '2개'). 정확한 수량 정보가 없으면 빈 문자열로 대답할 것.\n" +
                    "- recipe는 상세하고 친절하게 설명해.";

    @Value("${google.gemini.url}")
    private String url;

    @Value("${google.gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public String analyzeFoodImage(String base64Image, String mimeType) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                        Map.of("text", FOOD_PROMPT),
                        Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64Image))
                )))
        );
        return call(body);
    }

    public String analyzeRecipe(String videoTitle) {
        String prompt = String.format(RECIPE_PROMPT_TEMPLATE, videoTitle);
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );
        return call(body);
    }

    private String call(Map<String, Object> body) {
        try {
            Map<String, Object> response = restTemplate.postForObject(url + apiKey, body, Map.class);
            return parseResponse(response);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Gemini API 호출 중 오류가 발생했습니다.");
        }
    }

    private String parseResponse(Map<String, Object> response) {
        try {
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "음식이아님";
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts   = (List<Map<String, Object>>) content.get("parts");
            return parts.get(0).get("text").toString().trim();
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini 응답 형식이 올바르지 않습니다.");
        }
    }
}