package com.restroute.service.image;

public class RestFoodNotFoundException extends RuntimeException {

    public RestFoodNotFoundException(Long foodId) {
        super("Rest food not found: " + foodId);
    }

    public static RestFoodNotFoundException forId(Long foodId) {
        return new RestFoodNotFoundException(foodId);
    }
}
