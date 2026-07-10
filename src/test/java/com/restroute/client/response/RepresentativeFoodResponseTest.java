package com.restroute.client.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepresentativeFoodResponseTest {

    @Test
    @DisplayName("대표 음식 응답의 실제 필드와 페이지 메타데이터를 역직렬화한다")
    void deserializeActualResponse() throws Exception {
        String json = """
                {
                  "list": [{
                    "direction": "부산",
                    "serviceAreaCode": "A00001",
                    "serviceAreaCode2": "000001",
                    "serviceAreaName": "서울만남(부산)휴게소",
                    "routeCode": "0010",
                    "routeName": "경부선",
                    "batchMenu": "말죽거리소고기국밥",
                    "salePrice": "￦6,000"
                  }],
                  "count": 221,
                  "pageNo": 1,
                  "numOfRows": 99,
                  "pageSize": 3,
                  "code": "SUCCESS",
                  "message": "인증키가 유효합니다."
                }
                """;

        RepresentativeFoodResponse response = new ObjectMapper().readValue(json, RepresentativeFoodResponse.class);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPageSize()).isEqualTo(3);
        assertThat(response.getList()).singleElement().satisfies(item -> {
            assertThat(item.getServiceAreaCode()).isEqualTo("A00001");
            assertThat(item.getServiceAreaCode2()).isEqualTo("000001");
            assertThat(item.getDirection()).isEqualTo("부산");
            assertThat(item.getBatchMenu()).isEqualTo("말죽거리소고기국밥");
            assertThat(item.getSalePrice()).isEqualTo("￦6,000");
        });
    }
}
