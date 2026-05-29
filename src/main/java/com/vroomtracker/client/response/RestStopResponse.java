package com.vroomtracker.client.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestStopResponse {

    private String code;
    private String message;
    private String count;
    private String pageSize;
    private List<RestStopItem> list;

    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }

    public int getTotalPageCount() {
        try {
            return Integer.parseInt(pageSize);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
