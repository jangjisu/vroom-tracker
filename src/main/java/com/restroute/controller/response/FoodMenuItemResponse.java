package com.restroute.controller.response;

import com.restroute.domain.RestFoodEntity;

public record FoodMenuItemResponse(
        String foodName,
        String foodCost,
        String description,
        boolean representative,
        boolean bestFood,
        boolean premium,
        String season,
        String seasonLabel) {

    public static FoodMenuItemResponse from(RestFoodEntity entity) {
        return new FoodMenuItemResponse(
                entity.getFoodName(),
                entity.getFoodCost(),
                entity.getDescription(),
                "Y".equals(entity.getRecommendYn()),
                "Y".equals(entity.getBestFoodYn()),
                "Y".equals(entity.getPremiumYn()),
                entity.getSeasonMenu(),
                seasonLabel(entity.getSeasonMenu()));
    }

    private static String seasonLabel(String season) {
        if ("4".equals(season)) {
            return "사계절";
        }
        if ("S".equals(season)) {
            return "여름";
        }
        if ("W".equals(season)) {
            return "겨울";
        }
        return null;
    }
}
