package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestStopDetailResponse {

    private String code;
    private String message;
    private String count;
    private String pageSize;
    private List<RestStopDetailItem> list;
    private UpstreamException exception;

    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }

    public String getErrorMessage() {
        if (exception == null) {
            return message;
        }

        return exception.message();
    }

    public int getTotalPageCount() {
        try {
            return Integer.parseInt(pageSize);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record UpstreamException(String message) {}
}
