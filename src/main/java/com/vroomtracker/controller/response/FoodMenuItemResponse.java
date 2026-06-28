package com.vroomtracker.controller.response;

import com.vroomtracker.domain.RestFoodEntity;

public record FoodMenuItemResponse(
        String foodName,
        String foodCost,
        String description,
        boolean representative,
        boolean bestFood,
        boolean premium,
        String season) {

    public static FoodMenuItemResponse from(RestFoodEntity entity) {
        return new FoodMenuItemResponse(
                entity.getFoodName(),
                entity.getFoodCost(),
                entity.getDescription(),
                "Y".equals(entity.getRecommendYn()),
                "Y".equals(entity.getBestFoodYn()),
                "Y".equals(entity.getPremiumYn()),
                entity.getSeasonMenu());
    }
}
