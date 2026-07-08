package com.restroute.service.route;

enum FuelType {
    GASOLINE("lowest-gasoline", "휘발유 최저가"),
    DIESEL("lowest-diesel", "경유 최저가"),
    LPG("lowest-lpg", "LPG 최저가");

    private final String tagKey;
    private final String tagLabel;

    FuelType(String tagKey, String tagLabel) {
        this.tagKey = tagKey;
        this.tagLabel = tagLabel;
    }

    String tagKey() {
        return tagKey;
    }

    String tagLabel() {
        return tagLabel;
    }
}
