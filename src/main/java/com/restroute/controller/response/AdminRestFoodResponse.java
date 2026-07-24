package com.restroute.controller.response;

import com.restroute.domain.RestFoodEntity;

public record AdminRestFoodResponse(
        Long id, String foodName, String foodCost, String description, boolean adminOverridden, boolean adminCreated) {

    public static AdminRestFoodResponse from(RestFoodEntity entity) {
        return new AdminRestFoodResponse(
                entity.getId(),
                entity.getFoodName(),
                entity.getFoodCost(),
                entity.getDescription(),
                entity.isAdminOverridden(),
                entity.isAdminCreated());
    }
}
