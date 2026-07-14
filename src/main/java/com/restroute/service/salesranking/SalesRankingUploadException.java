package com.restroute.service.salesranking;

public class SalesRankingUploadException extends RuntimeException {

    public static SalesRankingUploadException of(String message) {
        return new SalesRankingUploadException(message);
    }

    public SalesRankingUploadException(String message) {
        super(message);
    }
}
