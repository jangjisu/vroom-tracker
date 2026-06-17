package com.vroomtracker.client;

public class KakaoApiException extends RuntimeException {

    public KakaoApiException(String requestDescription, String message) {
        super(buildMessage(requestDescription, message));
    }

    public KakaoApiException(String requestDescription, String message, Throwable cause) {
        super(buildMessage(requestDescription, message), cause);
    }

    private static String buildMessage(String requestDescription, String message) {
        return "Failed to call Kakao API. request=" + requestDescription + ", message=" + message;
    }
}
