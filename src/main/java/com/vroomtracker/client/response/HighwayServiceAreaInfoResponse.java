package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HighwayServiceAreaInfoResponse {

    private String code;
    private String message;
    private String count;
    private List<HighwayServiceAreaInfoItem> list;

    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }
}
