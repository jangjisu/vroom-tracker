package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TrafficIcResponse {
    private String code;
    private String message;
    private String count;
    /** ※ 실제 응답의 list 필드명을 확인 후 수정하세요 */
    @JsonProperty("list")
    private List<TrafficIcItem> list;

    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }
}
