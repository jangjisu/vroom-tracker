package com.vroomtracker.util;

import java.time.format.DateTimeFormatter;

public class TrafficUtils {

    private TrafficUtils() {}

    public static double parseAmount(String amount) {
        try {
            return Double.parseDouble(amount.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String formatSumTm(String sumTm) {
        if (sumTm == null || sumTm.length() < 10) return sumTm;
        try {
            if (sumTm.length() == 10) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH시")
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHH").parse(sumTm));
            }
            if (sumTm.length() == 12) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm").parse(sumTm));
            }
        } catch (Exception ignored) {}
        return sumTm;
    }
}
