package com.vroomtracker.controller.response;

import com.vroomtracker.domain.RestFoodEntity;

public record FoodMenuItemResponse(String foodName, String foodCost, String description, boolean representative) {

    public static FoodMenuItemResponse from(RestFoodEntity entity) {
        return new FoodMenuItemResponse(
                entity.getFoodName(),
                entity.getFoodCost(),
                entity.getDescription(),
                "Y".equals(entity.getRecommendYn()));
    }
}
