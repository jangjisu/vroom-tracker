package com.vroomtracker.client;

/** trafficIc API tmType 파라미터 — 집계 단위 */
public enum TmType {
    ONE_HOUR("1"),
    FIFTEEN_MIN("2"),
    FIVE_MIN("3");

    private final String value;

    TmType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
