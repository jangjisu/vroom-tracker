package com.restroute.client.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepresentativeFoodResponseTest {

    @Test
    @DisplayName("대표 음식 API 응답의 페이지와 대표 음식 필드를 매핑한다")
    void readValue_mapsPaginationAndRepresentativeFoodFields() throws Exception {
        String json = """
                {
                  "count": 221,
                  "pageNo": 1,
                  "numOfRows": 99,
                  "pageSize": 3,
                  "message": "인증키가 유효합니다.",
                  "code": "SUCCESS",
                  "list": [
                    {
                      "serviceAreaCode": "A00001",
                      "serviceAreaCode2": "000001",
                      "serviceAreaName": "서울만남(부산)휴게소",
                      "routeCode": "0010",
                      "routeName": "경부선",
                      "direction": "부산",
                      "batchMenu": "말죽거리소고기국밥",
                      "salePrice": "￦6,000"
                    }
                  ]
                }
                """;

        RepresentativeFoodResponse response = new ObjectMapper().readValue(json, RepresentativeFoodResponse.class);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCount()).isEqualTo(221);
        assertThat(response.getPageNo()).isEqualTo(1);
        assertThat(response.getNumOfRows()).isEqualTo(99);
        assertThat(response.getPageSize()).isEqualTo(3);
        assertThat(response.getTotalPageCount()).isEqualTo(3);
        assertThat(response.getList()).hasSize(1);
        assertThat(response.getList().get(0).getServiceAreaCode()).isEqualTo("A00001");
        assertThat(response.getList().get(0).getServiceAreaCode2()).isEqualTo("000001");
        assertThat(response.getList().get(0).getDirection()).isEqualTo("부산");
        assertThat(response.getList().get(0).getBatchMenu()).isEqualTo("말죽거리소고기국밥");
        assertThat(response.getList().get(0).getSalePrice()).isEqualTo("￦6,000");
    }

    @Test
    @DisplayName("대표 음식과 가격이 없는 행도 응답으로 매핑한다")
    void readValue_allowsMissingOptionalFields() throws Exception {
        String json = """
                {
                  "code": "SUCCESS",
                  "list": [
                    {
                      "serviceAreaCode": "A00058"
                    }
                  ]
                }
                """;

        RepresentativeFoodResponse response = new ObjectMapper().readValue(json, RepresentativeFoodResponse.class);

        assertThat(response.getList().get(0).getServiceAreaCode()).isEqualTo("A00058");
        assertThat(response.getList().get(0).getDirection()).isNull();
        assertThat(response.getList().get(0).getBatchMenu()).isNull();
        assertThat(response.getList().get(0).getSalePrice()).isNull();
        assertThat(response.getTotalPageCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("SUCCESS 외 코드는 실패로 판단하고 오류 메시지를 반환한다")
    void isSuccess_returnsFalseForErrorCode() throws Exception {
        RepresentativeFoodResponse response = new ObjectMapper()
                .readValue("{\"code\":\"ERROR\",\"message\":\"인증키가 유효하지 않습니다.\"}", RepresentativeFoodResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).isEqualTo("인증키가 유효하지 않습니다.");
    }
}
