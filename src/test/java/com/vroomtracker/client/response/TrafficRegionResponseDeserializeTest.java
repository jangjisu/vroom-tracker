package com.vroomtracker.client.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficRegionResponseDeserializeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialize_trafficRegionField_mapsToList() throws Exception {
        String json = """
                {
                  "code": "SUCCESS",
                  "message": "인증키가 유효합니다.",
                  "count": "1",
                  "trafficRegion": [
                    {
                      "regionCode": "927",
                      "regionName": "전북본부",
                      "trafficAmout": "100",
                      "tcsType": "1",
                      "carType": "1",
                      "openClType": "0",
                      "exDivCode": "00",
                      "inoutType": "1",
                      "tmType": "2",
                      "sumTm": "0900",
                      "sumDate": "20260313"
                    }
                  ]
                }
                """;

        TrafficRegionResponse response = mapper.readValue(json, TrafficRegionResponse.class);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getList()).isNotNull().hasSize(1);
        assertThat(response.getList().get(0).getRegionCode()).isEqualTo("927");
        assertThat(response.getList().get(0).getSumDate()).isEqualTo("20260313");
    }
}
