package com.restroute.domain;

import static com.restroute.support.RestStopTestFixtures.restStopDetailItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.restroute.client.response.RestStopDetailItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestStopDetailEntityTest {

    @Test
    @DisplayName("외부 API 휴게소 상세 item을 entity로 변환한다")
    void from_convertsRestStopDetailItem() {
        RestStopDetailItem item = restStopDetailItem("A00078", "건천(부산)휴게소");

        RestStopDetailEntity entity = RestStopDetailEntity.from(item);

        assertThat(entity.getRouteName()).isEqualTo("경부선");
        assertThat(entity.getServiceAreaCode()).isEqualTo("A00078");
        assertThat(entity.getServiceAreaName()).isEqualTo("건천(부산)휴게소");
        assertThat(entity.getTelNo()).isEqualTo("054-751-6890");
        assertThat(entity.getBrand()).isEqualTo("투썸플레이스");
        assertThat(entity.getRouteCode()).isEqualTo("0010");
        assertThat(entity.getServiceAreaCode2()).isEqualTo("000054");
        assertThat(entity.getSvarAddr()).isEqualTo("경북 경주시 건천읍방내리 14");
        assertThat(entity.getConvenience()).isEqualTo("수유실");
        assertThat(entity.getMaintenanceYn()).isEqualTo("X");
        assertThat(entity.getTruckSaYn()).isEqualTo("X");
    }
}
