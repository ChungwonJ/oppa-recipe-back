package org.example.domain.youtube.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.domain.youtube.dto.response.RecipeAnalysisResponse;
import org.example.domain.youtube.dto.response.YoutubeSearchResultResponse;
import org.example.global.exception.ServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    @Value("${google.gemini.url}")
    private String geminiUrl;

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public YoutubeSearchResultResponse getMostLikedShorts(String foodName) {
        try {
            String searchUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("part", "snippet")
                    .queryParam("q", "집에서 간단하게 만드는 " + foodName + " 레시피 shorts")
                    .queryParam("type", "video")
                    .queryParam("videoDuration", "short")
                    .queryParam("maxResults", 5)
                    .queryParam("key", youtubeApiKey)
                    .build().toUriString();

            JsonNode searchResponse = restTemplate.getForObject(searchUrl, JsonNode.class);
            if (searchResponse == null || searchResponse.path("items").isEmpty()) return null;

            Map<String, String> idToTitleMap = new HashMap<>();
            List<String> videoIds = new ArrayList<>();

            searchResponse.path("items").forEach(item -> {
                String videoId = item.path("id").path("videoId").asText();
                String title = item.path("snippet").path("title").asText();
                videoIds.add(videoId);
                idToTitleMap.put(videoId, title);
            });

            String statsUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                    .queryParam("part", "statistics")
                    .queryParam("id", String.join(",", videoIds))
                    .queryParam("key", youtubeApiKey)
                    .build().toUriString();

            JsonNode statsResponse = restTemplate.getForObject(statsUrl, JsonNode.class);

            return StreamSupport.stream(statsResponse.path("items").spliterator(), false)
                    .max(Comparator.comparingLong(v -> v.path("statistics").path("likeCount").asLong(0)))
                    .map(v -> {
                        String id = v.path("id").asText();
                        return new YoutubeSearchResultResponse(
                                "https://www.youtube.com/shorts/" + id,
                                idToTitleMap.get(id)
                        );
                    })
                    .orElse(null);
        } catch (Exception e) {
            throw new ServerException("유튜브 API 호출 실패: " + e.getMessage());
        }
    }

    public RecipeAnalysisResponse analyzeRecipe(YoutubeSearchResultResponse searchResult, String foodName) {
        String fullUrl = geminiUrl + geminiApiKey;

        String prompt = String.format(
                "영상 제목: %s\n" +
                        "위 요리 영상의 내용을 분석해서 반드시 아래 JSON 형식으로만 답변해. 다른 설명은 하지마.\n\n" +
                        "{\n" +
                        "  \"recipe\": \"1. 조리법... 2. 조리법...\",\n" +
                        "  \"ingredients\": [\n" +
                        "    {\"name\": \"재료이름\", \"amount\": \"수량\"},\n" +
                        "    {\"name\": \"재료이름\", \"amount\": \"수량\"}\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "조건:\n" +
                        "- 'name'은 쿠팡 검색용이므로 '당근', '계란'처럼 명사만 쓸 것.\n" +
                        "- 'amount': 그램수나 갯수 (예: '300g', '2개'). **정확한 수량 정보가 없으면 빈 문자열(\"\")로 대답할 것.**\n" +
                        "- recipe는 상세하고 친절하게 설명해.",
                searchResult.getTitle());

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            JsonNode response = restTemplate.postForObject(fullUrl, requestBody, JsonNode.class);
            String rawText = response.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            String jsonStr = rawText.replaceAll("```json|```", "").trim();
            JsonNode recipeNode = objectMapper.readTree(jsonStr);

            List<RecipeAnalysisResponse.IngredientDto> ingredientList = new ArrayList<>();
            recipeNode.path("ingredients").forEach(i -> {
                String name = i.path("name").asText("").trim();
                String amount = i.path("amount").asText("").trim();

                if (amount.equalsIgnoreCase("null") || amount.equals("정보없음") || amount.isEmpty()) {
                    amount = "";
                }

                if (!name.isEmpty()) {
                    ingredientList.add(new RecipeAnalysisResponse.IngredientDto(name, amount));
                }
            });

            return RecipeAnalysisResponse.builder()
                    .foodName(foodName)
                    .videoTitle(searchResult.getTitle())
                    .shortsUrl(searchResult.getUrl())
                    .recipe(recipeNode.path("recipe").asText())
                    .ingredients(ingredientList)
                    .build();

        } catch (Exception e) {
            throw new ServerException("레시피 요약 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}