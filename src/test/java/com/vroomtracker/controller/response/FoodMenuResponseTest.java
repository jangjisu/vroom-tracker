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
    @DisplayName("음식 메뉴 구분 값을 화면용 필드로 변환한다")
    void from_mapsMenuKinds() throws Exception {
        RestFoodEntity highlighted = foodEntity("농심어묵우동", "7000", "시원한 우동", "Y", "Y", "Y", "S");
        RestFoodEntity normal = foodEntity("한우국밥", "9000", "든든한 국밥", "N", "N", "N", "4");

        FoodMenuResponse response = FoodMenuResponse.from(List.of(highlighted, normal));

        assertThat(response.menus())
                .extracting("foodName", "foodCost", "description", "representative", "bestFood", "premium", "season")
                .containsExactly(
                        tuple("농심어묵우동", "7000", "시원한 우동", true, true, true, "S"),
                        tuple("한우국밥", "9000", "든든한 국밥", false, false, false, "4"));
    }

    @Test
    @DisplayName("음식 메뉴가 없으면 빈 목록을 반환한다")
    void from_returnsEmptyWhenNoFoods() {
        FoodMenuResponse response = FoodMenuResponse.from(List.of());

        assertThat(response.menus()).isEmpty();
    }

    private RestFoodEntity foodEntity(
            String foodNm,
            String foodCost,
            String etc,
            String recommendYn,
            String bestFoodYn,
            String premiumYn,
            String seasonMenu)
            throws Exception {
        String json = """
                {
                  "foodNm": "%s",
                  "foodCost": "%s",
                  "etc": "%s",
                  "recommendyn": "%s",
                  "bestfoodyn": "%s",
                  "premiumyn": "%s",
                  "seasonMenu": "%s"
                }
                """.formatted(foodNm, foodCost, etc, recommendYn, bestFoodYn, premiumYn, seasonMenu);
        return RestFoodEntity.from(new ObjectMapper().readValue(json, RestBestfoodItem.class));
    }
}
