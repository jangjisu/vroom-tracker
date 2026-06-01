package com.vroomtracker.controller.response;

public record MapConfigResponse(String naverMapsNcpKeyId) {

    public static MapConfigResponse of(String naverMapsNcpKeyId) {
        return new MapConfigResponse(naverMapsNcpKeyId);
    }
}
