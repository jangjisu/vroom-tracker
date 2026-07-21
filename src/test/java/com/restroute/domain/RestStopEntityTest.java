package com.restroute.domain;

import static com.restroute.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.restroute.client.response.RestStopItem;
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

    @Test
    @DisplayName("관리자 편집을 적용하면 필드가 바뀌고 동기화 잠금 플래그가 켜진다")
    void applyAdminEdit_updatesFieldsAndSetsOverrideFlag() {
        RestStopEntity entity = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));

        entity.applyAdminEdit("이름수정됨", "0011", "노선수정됨", "128.0", "38.0");

        assertThat(entity.getUnitName()).isEqualTo("이름수정됨");
        assertThat(entity.getRouteNo()).isEqualTo("0011");
        assertThat(entity.getRouteName()).isEqualTo("노선수정됨");
        assertThat(entity.getXValue()).isEqualTo("128.0");
        assertThat(entity.getYValue()).isEqualTo("38.0");
        assertThat(entity.isAdminOverridden()).isTrue();
    }

    @Test
    @DisplayName("clearAdminOverride를 호출하면 잠금 플래그가 꺼진다")
    void clearAdminOverride_resetsOverrideFlag() {
        RestStopEntity entity = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        entity.applyAdminEdit("이름수정됨", "0011", "노선수정됨", "128.0", "38.0");

        entity.clearAdminOverride();

        assertThat(entity.isAdminOverridden()).isFalse();
    }
}
