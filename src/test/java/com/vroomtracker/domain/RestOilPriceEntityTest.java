package com.vroomtracker.domain;

import static com.vroomtracker.support.RestStopTestFixtures.restOilPriceItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.vroomtracker.client.response.RestOilPriceItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestOilPriceEntityTest {

    @Test
    @DisplayName("주유소 가격 API 원본 필드를 문자열 그대로 저장한다")
    void from_mapsOriginalFields() {
        RestOilPriceItem item = restOilPriceItem("000002", "서울만남(부산)주유소");

        RestOilPriceEntity entity = RestOilPriceEntity.from(item);

        assertThat(entity.getRouteCode()).isEqualTo("0010");
        assertThat(entity.getServiceAreaCode()).isEqualTo("B00001");
        assertThat(entity.getRouteName()).isEqualTo("경부선");
        assertThat(entity.getDirection()).isEqualTo("부산");
        assertThat(entity.getOilCompany()).isEqualTo("AD");
        assertThat(entity.getLpgYn()).isEqualTo("Y");
        assertThat(entity.getServiceAreaName()).isEqualTo("서울만남(부산)주유소");
        assertThat(entity.getTelNo()).isEqualTo("02-573-7430");
        assertThat(entity.getGasolinePrice()).isEqualTo("1,999원");
        assertThat(entity.getDieselPrice()).isEqualTo("1,997원");
        assertThat(entity.getLpgPrice()).isEqualTo("1,157원");
        assertThat(entity.getNumOfRows()).isNull();
        assertThat(entity.getPageNo()).isNull();
        assertThat(entity.getServiceAreaCode2()).isEqualTo("000002");
        assertThat(entity.getServiceAreaAddress()).isEqualTo("서울시 서초구 원지동10-16");
    }

    @Test
    @DisplayName("기존 주유소 가격 정보를 API 원본 필드로 갱신한다")
    void updateFrom_updatesOriginalFields() {
        RestOilPriceEntity entity = RestOilPriceEntity.from(restOilPriceItem("000002", "서울만남(부산)주유소"));
        RestOilPriceItem changed = restOilPriceItem("000002", "서울만남(부산)주유소");
        org.springframework.test.util.ReflectionTestUtils.setField(changed, "oilCompany", "SK");
        org.springframework.test.util.ReflectionTestUtils.setField(changed, "gasolinePrice", "1,888원");
        org.springframework.test.util.ReflectionTestUtils.setField(changed, "dieselPrice", "1,777원");
        org.springframework.test.util.ReflectionTestUtils.setField(changed, "lpgPrice", "X");
        org.springframework.test.util.ReflectionTestUtils.setField(changed, "telNo", "02-000-0000");

        entity.updateFrom(changed);

        assertThat(entity.getOilCompany()).isEqualTo("SK");
        assertThat(entity.getGasolinePrice()).isEqualTo("1,888원");
        assertThat(entity.getDieselPrice()).isEqualTo("1,777원");
        assertThat(entity.getLpgPrice()).isEqualTo("X");
        assertThat(entity.getTelNo()).isEqualTo("02-000-0000");
    }
}
