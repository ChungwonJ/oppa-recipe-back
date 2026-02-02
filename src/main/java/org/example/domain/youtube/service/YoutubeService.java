package org.example.domain.youtube.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
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

    public YoutubeSearchResultResponse getMostLikedShorts(String foodName) {
        try {
            String searchUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("part", "snippet")
                    .queryParam("q", "집에서 간단하게 만드는" + foodName + " 레시피 shorts")
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

    public String analyzeRecipe(YoutubeSearchResultResponse searchResult) {

        String fullUrl = geminiUrl + geminiApiKey;

        String prompt = String.format(
                "영상 제목: %s\n" +
                        "영상 링크: %s\n\n" +
                        "위 영상의 내용을 분석해서 레시피를 요약해줘.\n" +
                        "레시피는 최대한 잘 설명해서 답변해.\n" +
                        "재료는 갯수랑 그람수도 포함해서 답변해.\n" +
                        "인사말이나 부연 설명은 모두 생략하고 아래 형식으로만 답변해.\n\n" +
                        "재료 : 재료1, 재료2, ...\n" +
                        "레시피\n" +
                        "1. 단계별 설명\n" +
                        "2. ...",
                searchResult.getTitle(), searchResult.getUrl());

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            JsonNode response = restTemplate.postForObject(fullUrl, requestBody, JsonNode.class);
            return response.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();
        } catch (Exception e) {
            throw new ServerException("레시피 요약 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}