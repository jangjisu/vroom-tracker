package com.vroomtracker.client;

/** trafficIc API inoutType 파라미터 — 입출구 구분 */
public enum InoutType {
    ENTRANCE("0"),
    EXIT("1");

    private final String value;

    InoutType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
