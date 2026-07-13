package com.restroute.service.evcharger.mapping;

public enum EvChargerMatchType {
    NAME_ADDRESS_DISTANCE("이름, 주소, 거리 일치"),
    NAME_DISTANCE("이름, 거리 일치"),
    ADDRESS_DISTANCE("주소, 거리 일치");

    private final String description;

    EvChargerMatchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
