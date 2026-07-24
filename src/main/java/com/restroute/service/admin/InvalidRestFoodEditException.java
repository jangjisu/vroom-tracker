package com.restroute.service.admin;

public class InvalidRestFoodEditException extends RuntimeException {

    public InvalidRestFoodEditException(String message) {
        super(message);
    }

    public static InvalidRestFoodEditException forSyncedFoodDeletion(Long foodId) {
        return new InvalidRestFoodEditException("Cannot delete a synced rest food row: " + foodId);
    }
}
