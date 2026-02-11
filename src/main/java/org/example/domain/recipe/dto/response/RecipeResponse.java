package org.example.domain.recipe.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.recipe.entity.Recipe;
import org.example.global.exception.ServerException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
public class RecipeResponse {
    private final Long id;

    private final String foodName;

    private final String videoTitle;

    private final String shortsUrl;

    private final String recipeContent;

    private final List<IngredientDto> ingredients;

    private final Instant createdAt;

    private final Instant updatedAt;

    private final Boolean isDeleted;

    @Builder
    private RecipeResponse(
            Long id,
            String foodName,
            String videoTitle,
            String shortsUrl,
            String recipeContent,
            List<IngredientDto> ingredients,
            Instant createdAt,
            Instant updatedAt,
            Boolean isDeleted
    ) {
        this.id = id;
        this.foodName = foodName;
        this.videoTitle = videoTitle;
        this.shortsUrl = shortsUrl;
        this.recipeContent = recipeContent;
        this.ingredients = ingredients;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    @Getter
    @NoArgsConstructor
    public static class IngredientDto {
        private String name;
        private String amount;

        @Builder
        public IngredientDto(String name, String amount) {
            this.name = name;
            this.amount = amount;
        }
    }

    public static RecipeResponse from(Recipe recipe, ObjectMapper objectMapper, String errorMessage) {
        List<IngredientDto> ingredientList = new ArrayList<>();
        try {
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                ingredientList = objectMapper.readValue(recipe.getIngredients(),
                        new TypeReference<List<IngredientDto>>() {});
            }
        } catch (Exception e) {
            throw new ServerException(errorMessage);
        }

        return RecipeResponse.builder()
                .id(recipe.getId())
                .foodName(recipe.getFoodName())
                .videoTitle(recipe.getVideoTitle())
                .shortsUrl(recipe.getShortsUrl())
                .recipeContent(recipe.getRecipeContent())
                .ingredients(ingredientList)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .isDeleted(recipe.getIsDeleted())
                .build();
    }
}