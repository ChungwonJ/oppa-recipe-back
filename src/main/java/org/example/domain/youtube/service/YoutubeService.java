package org.example.domain.youtube.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.example.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    private final RestTemplate restTemplate;

    public String getMostLikedShorts(String foodName) {
        try {
            // 1. UriComponentsBuilder로 안전하게 URL 생성
            String searchUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("part", "snippet")
                    .queryParam("q", foodName + " 레시피 shorts")
                    .queryParam("type", "video")
                    .queryParam("videoDuration", "short")
                    .queryParam("maxResults", 5)
                    .queryParam("key", youtubeApiKey)
                    .build().toUriString();

            JsonNode searchResponse = restTemplate.getForObject(searchUrl, JsonNode.class);

            if (searchResponse == null || searchResponse.path("items").isEmpty()) {
                return null;
            }

            // 2. ID 추출 (Stream 활용)
            List<String> videoIds = new ArrayList<>();
            searchResponse.path("items").forEach(item ->
                    videoIds.add(item.path("id").path("videoId").asText())
            );

            // 3. 통계 정보 가져오기
            String statsUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                    .queryParam("part", "statistics")
                    .queryParam("id", String.join(",", videoIds))
                    .queryParam("key", youtubeApiKey)
                    .build().toUriString();

            JsonNode statsResponse = restTemplate.getForObject(statsUrl, JsonNode.class);
            JsonNode videoItems = statsResponse.path("items");

            // 4. 최다 좋아요 영상 찾기
            String bestVideoId = StreamSupport.stream(videoItems.spliterator(), false)
                    .max(Comparator.comparingLong(v ->
                            v.path("statistics").path("likeCount").asLong(0) // 비공개시 0
                    ))
                    .map(v -> v.path("id").asText())
                    .orElse(null);

            return bestVideoId != null ? "https://www.youtube.com/shorts/" + bestVideoId : null;

        } catch (Exception e) {
            // 로깅 추가 권장: log.error("Youtube API Error", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "유튜브 정보 조회 중 오류가 발생했습니다.");
        }
    }
}