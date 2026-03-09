package com.vroomtracker.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.name(), ResponseCode.SUCCESS.getDefaultMessage(), data);
    }

    public static ApiResponse<Void> error(ResponseCode code) {
        return new ApiResponse<>(code.name(), code.getDefaultMessage(), null);
    }

    public static ApiResponse<Void> error(ResponseCode code, String message) {
        return new ApiResponse<>(code.name(), message, null);
    }
}
