package com.vroomtracker.domain;

import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.vroomtracker.client.response.RestStopItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestStopEntityTest {

    @Test
    @DisplayName("외부 API 휴게소 item을 entity로 변환한다")
    void from_convertsRestStopItem() {
        RestStopItem item = restStopItem("001", "서울만남(부산)휴게소");

        RestStopEntity entity = RestStopEntity.from(item);

        assertThat(entity.getUnitCode()).isEqualTo("001");
        assertThat(entity.getUnitName()).isEqualTo("서울만남(부산)휴게소");
        assertThat(entity.getRouteNo()).isEqualTo("0010");
        assertThat(entity.getRouteName()).isEqualTo("경부선");
        assertThat(entity.getXValue()).isEqualTo("127.042514");
        assertThat(entity.getYValue()).isEqualTo("37.459939");
        assertThat(entity.getStdRestCd()).isEqualTo("000001");
        assertThat(entity.getServiceAreaCode()).isEqualTo("A00001");
    }
}
