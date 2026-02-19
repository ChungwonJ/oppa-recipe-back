package org.example.domain.recipe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.user.entity.User;
import org.example.global.base.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE recipes SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "recipes")
public class Recipe extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private String videoTitle;

    @Column(nullable = false)
    private String shortsUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recipeContent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ingredients;

    @Builder
    private Recipe(
            User user,
            String foodName,
            String videoTitle,
            String shortsUrl,
            String recipeContent,
            String ingredients
    ) {
        this.user = user;
        this.foodName = foodName;
        this.videoTitle = videoTitle;
        this.shortsUrl = shortsUrl;
        this.recipeContent = recipeContent;
        this.ingredients = ingredients;
    }

    public void update(String recipeContent, String ingredients) {
        this.recipeContent = recipeContent;
        this.ingredients = ingredients;
    }
}
