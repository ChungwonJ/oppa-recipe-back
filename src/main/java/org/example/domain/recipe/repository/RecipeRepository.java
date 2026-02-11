package org.example.domain.recipe.repository;

import org.example.domain.recipe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Recipe> findAllByUserId(Long userId, Pageable pageable);
    Optional<Recipe> findById(Long id);
}
