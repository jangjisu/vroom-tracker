package com.vroomtracker.domain;

public enum CongestionLevel {

    HIGH(5.0, "많음"),
    MEDIUM(2.0, "보통"),
    LOW(0.0, "적음");

    private final double threshold;
    private final String label;

    CongestionLevel(double threshold, String label) {
        this.threshold = threshold;
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static CongestionLevel from(double vol) {
        if (vol >= HIGH.threshold) return HIGH;
        if (vol >= MEDIUM.threshold) return MEDIUM;
        return LOW;
    }
}
