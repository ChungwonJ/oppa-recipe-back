package org.example.domain.youtube.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YoutubeSearchResultResponse {
    private String url;
    private String title;
}
