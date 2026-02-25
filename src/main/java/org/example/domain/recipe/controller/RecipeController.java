package org.example.domain.recipe.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.recipe.dto.request.RecipeSaveRequest;
import org.example.domain.recipe.dto.response.RecipeResponse;
import org.example.domain.recipe.service.RecipeService;
import org.example.global.base.ApiResponse;
import org.example.global.base.Pagecond;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> saveRecipe(
            @RequestBody RecipeSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long recipeId = recipeService.save(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(recipeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<List<RecipeResponse>>> getMyRecipes(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute Pagecond pagecond) {

        ApiResponse<List<RecipeResponse>> response =
                recipeService.getMyRecipes(userDetails.getUsername(), pagecond);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        recipeService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
