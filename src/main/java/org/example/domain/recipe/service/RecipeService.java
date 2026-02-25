package org.example.domain.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.domain.recipe.dto.request.RecipeSaveRequest;
import org.example.domain.recipe.dto.response.RecipeResponse;
import org.example.domain.recipe.entity.Recipe;
import org.example.domain.recipe.repository.RecipeRepository;
import org.example.domain.user.entity.User;
import org.example.domain.user.repository.UserRepository;
import org.example.global.base.ApiResponse;
import org.example.global.base.PageInfo;
import org.example.global.base.Pagecond;
import org.example.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long save(RecipeSaveRequest request, String naverId) {
        User user = userRepository.findByNaverIdAndIsDeletedFalse(naverId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Recipe recipe = Recipe.builder()
                .user(user)
                .foodName(request.getFoodName())
                .videoTitle(request.getVideoTitle())
                .shortsUrl(request.getShortsUrl())
                .recipeContent(request.getRecipeContent())
                .ingredients(request.getIngredients())
                .build();

        return recipeRepository.save(recipe).getId();
    }

    public ApiResponse<List<RecipeResponse>> getMyRecipes(String naverId, Pagecond pagecond) {
        User user = userRepository.findByNaverIdAndIsDeletedFalse(naverId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        int pageIdx = Math.max(pagecond.getPageNum() - 1, 0);
        Pageable pageable = PageRequest.of(pageIdx, pagecond.getPageSize(), Sort.by("createdAt").descending());

        Page<Recipe> recipePage = recipeRepository.findAllByUserId(user.getId(), pageable);

        List<RecipeResponse> content = recipePage.getContent().stream()
                .map(recipe -> RecipeResponse.from(recipe, objectMapper, "레시피 변환 중 오류가 발생했습니다."))
                .collect(Collectors.toList());

        PageInfo pageInfo = PageInfo.builder()
                .pageNum(pagecond.getPageNum())
                .pageSize(recipePage.getSize())
                .totalElement(recipePage.getTotalElements())
                .totalPage(recipePage.getTotalPages())
                .build();

        return ApiResponse.of(content, pageInfo);
    }

    public ApiResponse<RecipeResponse> getRecipeDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다."));

        return ApiResponse.of(RecipeResponse.from(recipe, objectMapper, "레시피 변환 중 오류 발생"));
    }

    @Transactional
    public void delete(Long recipeId, String naverId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다."));

        if (!recipe.getUser().getNaverId().equals(naverId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        recipeRepository.delete(recipe);
    }
}