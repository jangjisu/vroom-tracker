package com.vroomtracker.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vroomtracker.client.response.RestBestfoodItem;
import com.vroomtracker.domain.RestFoodEntity;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FoodMenuResponseTest {

    @Test
    @DisplayName("추천 메뉴는 대표로 표시하고 전체 메뉴를 순서대로 변환한다")
    void from_marksRecommendedAsRepresentative() throws Exception {
        RestFoodEntity recommended = foodEntity("농심어묵우동", "7000", "시원한 우동", "Y");
        RestFoodEntity normal = foodEntity("한우국밥", "9000", "든든한 국밥", "N");

        FoodMenuResponse response = FoodMenuResponse.from(List.of(recommended, normal));

        assertThat(response.menus())
                .extracting("foodName", "foodCost", "description", "representative")
                .containsExactly(tuple("농심어묵우동", "7000", "시원한 우동", true), tuple("한우국밥", "9000", "든든한 국밥", false));
    }

    @Test
    @DisplayName("음식 메뉴가 없으면 빈 목록을 반환한다")
    void from_returnsEmptyWhenNoFoods() {
        FoodMenuResponse response = FoodMenuResponse.from(List.of());

        assertThat(response.menus()).isEmpty();
    }

    private RestFoodEntity foodEntity(String foodNm, String foodCost, String etc, String recommendYn) throws Exception {
        String json = "{\"foodNm\":\"%s\",\"foodCost\":\"%s\",\"etc\":\"%s\",\"recommendyn\":\"%s\"}"
                .formatted(foodNm, foodCost, etc, recommendYn);
        return RestFoodEntity.from(new ObjectMapper().readValue(json, RestBestfoodItem.class));
    }
}
