package org.example.domain.recipe.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecipeSaveRequest {
    private String foodName;
    private String videoTitle;
    private String shortsUrl;
    private String recipeContent;
    private String ingredients;
}
