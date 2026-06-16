package com.vroomtracker.controller.response;

import com.vroomtracker.domain.RestFoodEntity;
import java.util.List;

public record FoodMenuResponse(List<FoodMenuItemResponse> menus) {

    public static FoodMenuResponse from(List<RestFoodEntity> foods) {
        return new FoodMenuResponse(
                foods.stream().map(FoodMenuItemResponse::from).toList());
    }
}
