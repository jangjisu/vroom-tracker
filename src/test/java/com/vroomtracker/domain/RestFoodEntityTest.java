package com.vroomtracker.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vroomtracker.client.response.RestBestfoodItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestFoodEntityTest {

    @Test
    @DisplayName("음식 메뉴 item을 원본 문자열 그대로 엔티티로 매핑한다")
    void from_mapsRestBestfoodItemFields() throws Exception {
        String json = """
                {
                  "stdRestCd": "000001",
                  "stdRestNm": "서울만남(부산)휴게소",
                  "restCd": "S000001",
                  "routeCd": "0010",
                  "routeNm": "경부선",
                  "svarAddr": "서울 서초구 원지동10-16",
                  "seq": "272",
                  "foodNm": "농심어묵우동",
                  "foodCost": "7000",
                  "etc": "부산어묵꼬치를 첨가한 우동.",
                  "foodMaterial": "냉동면 1ea",
                  "recommendyn": "Y",
                  "bestfoodyn": "N",
                  "premiumyn": "N",
                  "seasonMenu": "4",
                  "app": "Y"
                }
                """;
        RestBestfoodItem item = new ObjectMapper().readValue(json, RestBestfoodItem.class);

        RestFoodEntity entity = RestFoodEntity.from(item);

        assertThat(entity.getStdRestCd()).isEqualTo("000001");
        assertThat(entity.getStdRestName()).isEqualTo("서울만남(부산)휴게소");
        assertThat(entity.getRestCode()).isEqualTo("S000001");
        assertThat(entity.getRouteCode()).isEqualTo("0010");
        assertThat(entity.getRouteName()).isEqualTo("경부선");
        assertThat(entity.getServiceAreaAddress()).isEqualTo("서울 서초구 원지동10-16");
        assertThat(entity.getSeq()).isEqualTo("272");
        assertThat(entity.getFoodName()).isEqualTo("농심어묵우동");
        assertThat(entity.getFoodCost()).isEqualTo("7000");
        assertThat(entity.getDescription()).isEqualTo("부산어묵꼬치를 첨가한 우동.");
        assertThat(entity.getFoodMaterial()).isEqualTo("냉동면 1ea");
        assertThat(entity.getRecommendYn()).isEqualTo("Y");
        assertThat(entity.getBestFoodYn()).isEqualTo("N");
        assertThat(entity.getPremiumYn()).isEqualTo("N");
        assertThat(entity.getSeasonMenu()).isEqualTo("4");
        assertThat(entity.getAppExposeYn()).isEqualTo("Y");
    }
}
