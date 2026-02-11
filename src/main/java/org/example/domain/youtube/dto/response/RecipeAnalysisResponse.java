package org.example.domain.youtube.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeAnalysisResponse {
    private String foodName;
    private String videoTitle;
    private String shortsUrl;
    private String recipe;
    private List<IngredientDto> ingredients;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IngredientDto {
        private String name;
        private String fullInfo;
    }
}
