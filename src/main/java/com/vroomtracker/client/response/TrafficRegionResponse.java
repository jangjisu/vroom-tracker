package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TrafficRegionResponse {
    private String code;
    private String message;
    private String count;
    @JsonProperty("trafficRegion")
    private List<TrafficRegionItem> list;

    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }
}
