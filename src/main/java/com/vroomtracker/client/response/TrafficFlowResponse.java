package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TrafficFlowResponse {
    private String code;
    private String message;
    private String count;
    @JsonProperty("trafficFlowByTimeLists")
    private List<TrafficFlowItem> list;

    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }
}
