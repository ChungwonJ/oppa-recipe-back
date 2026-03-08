package org.example.domain.recipe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.domain.gemini.client.GeminiClient;
import org.example.domain.youtube.client.YoutubeClient;
import org.example.domain.youtube.dto.response.RecipeAnalysisResponse;
import org.example.domain.youtube.dto.response.YoutubeSearchResultResponse;
import org.example.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeCrawlService {

    private final YoutubeClient youtubeClient;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public RecipeAnalysisResponse collect(String foodName) {
        YoutubeSearchResultResponse searchResult = youtubeClient.getMostLikedShorts(foodName);
        if (searchResult == null) {
            throw new CustomException(HttpStatus.NOT_FOUND,
                    foodName + "에 대한 적절한 레시피 영상을 찾을 수 없습니다.");
        }

        String rawText = geminiClient.analyzeRecipe(searchResult.getTitle());
        JsonNode recipeNode = parseJson(rawText);

        return RecipeAnalysisResponse.builder()
                .foodName(foodName)
                .videoTitle(searchResult.getTitle())
                .shortsUrl(searchResult.getUrl())
                .recipe(recipeNode.path("recipe").asText())
                .ingredients(parseIngredients(recipeNode))
                .build();
    }

    private JsonNode parseJson(String rawText) {
        try {
            String cleaned = rawText.replaceAll("```json|```", "").trim();
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "레시피 파싱에 실패했습니다.");
        }
    }

    private List<RecipeAnalysisResponse.IngredientDto> parseIngredients(JsonNode recipeNode) {
        List<RecipeAnalysisResponse.IngredientDto> ingredients = new ArrayList<>();
        recipeNode.path("ingredients").forEach(i -> {
            String name   = i.path("name").asText("").trim();
            String amount = normalizeAmount(i.path("amount").asText("").trim());
            if (!name.isEmpty()) {
                ingredients.add(new RecipeAnalysisResponse.IngredientDto(name, amount));
            }
        });
        return ingredients;
    }

    private String normalizeAmount(String amount) {
        if (amount.equalsIgnoreCase("null") || amount.equals("정보없음") || amount.isEmpty()) return "";
        return amount;
    }
}