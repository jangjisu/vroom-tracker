package com.restroute.client;

import java.util.Set;

final class ExternalApiRequestLog {

    private static final Set<String> SENSITIVE_PARAMETERS = Set.of("key", "code");

    private ExternalApiRequestLog() {}

    static String sanitizeUrl(String requestUrl) {
        if (requestUrl == null || requestUrl.isBlank()) {
            return requestUrl;
        }

        String sanitized = requestUrl;
        for (String parameter : SENSITIVE_PARAMETERS) {
            sanitized = sanitized.replaceAll("([?&])" + parameter + "=[^&]*", "$1" + parameter + "=<redacted>");
        }
        return sanitized;
    }
}
