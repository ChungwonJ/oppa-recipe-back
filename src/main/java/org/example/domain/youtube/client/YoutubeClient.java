package org.example.domain.youtube.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.example.domain.youtube.dto.response.YoutubeSearchResultResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class YoutubeClient {

    private static final String SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String STATS_URL  = "https://www.googleapis.com/youtube/v3/videos";

    @Value("${youtube.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public YoutubeSearchResultResponse getMostLikedShorts(String foodName) {
        JsonNode searchResponse = fetchSearchResults(foodName);
        if (searchResponse == null || searchResponse.path("items").isEmpty()) return null;

        Map<String, String> idToTitleMap = new HashMap<>();
        List<String> videoIds = extractVideoIds(searchResponse, idToTitleMap);

        JsonNode statsResponse = fetchVideoStats(videoIds);
        if (statsResponse == null) return null;

        return findMostLikedVideo(statsResponse, idToTitleMap);
    }

    private JsonNode fetchSearchResults(String foodName) {
        String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                .queryParam("part", "snippet")
                .queryParam("q", "집에서 간단하게 만드는 " + foodName + " 레시피 shorts")
                .queryParam("type", "video")
                .queryParam("videoDuration", "short")
                .queryParam("maxResults", 5)
                .queryParam("key", apiKey)
                .build().toUriString();
        return restTemplate.getForObject(url, JsonNode.class);
    }

    private List<String> extractVideoIds(JsonNode searchResponse, Map<String, String> idToTitleMap) {
        List<String> videoIds = new ArrayList<>();
        searchResponse.path("items").forEach(item -> {
            String videoId = item.path("id").path("videoId").asText();
            String title   = item.path("snippet").path("title").asText();
            videoIds.add(videoId);
            idToTitleMap.put(videoId, title);
        });
        return videoIds;
    }

    private JsonNode fetchVideoStats(List<String> videoIds) {
        String url = UriComponentsBuilder.fromHttpUrl(STATS_URL)
                .queryParam("part", "statistics")
                .queryParam("id", String.join(",", videoIds))
                .queryParam("key", apiKey)
                .build().toUriString();
        return restTemplate.getForObject(url, JsonNode.class);
    }

    private YoutubeSearchResultResponse findMostLikedVideo(
            JsonNode statsResponse, Map<String, String> idToTitleMap) {
        return StreamSupport.stream(statsResponse.path("items").spliterator(), false)
                .max(Comparator.comparingLong(v -> v.path("statistics").path("likeCount").asLong(0)))
                .map(v -> {
                    String id = v.path("id").asText();
                    return new YoutubeSearchResultResponse(
                            "https://www.youtube.com/shorts/" + id,
                            idToTitleMap.get(id));
                })
                .orElse(null);
    }
}
