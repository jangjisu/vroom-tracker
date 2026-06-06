package com.vroomtracker.controller.response;

import static com.vroomtracker.support.RestStopTestFixtures.highwayServiceAreaInfoItem;
import static com.vroomtracker.support.RestStopTestFixtures.restStopDetailItem;
import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.vroomtracker.client.response.HighwayServiceAreaInfoItem;
import com.vroomtracker.client.response.RestStopDetailItem;
import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import com.vroomtracker.domain.RestStopDetailEntity;
import com.vroomtracker.domain.RestStopEntity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RestStopDetailViewResponseTest {

    @Test
    @DisplayName("조회한 엔티티들로 휴게소 상세 응답 값을 구성한다")
    void of_returnsComposedResponse() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        RestStopDetailEntity detail = restStopDetail("경기 성남시", "수유실|쉼터", "O", "X");
        HighwayServiceAreaInfoEntity firstInfo = highwayServiceAreaInfo("10", "20", "1");
        HighwayServiceAreaInfoEntity secondInfo = highwayServiceAreaInfo("5", "7", "");

        RestStopDetailViewResponse response =
                RestStopDetailViewResponse.of(restStop, Optional.of(detail), List.of(firstInfo, secondInfo));

        assertThat(response.serviceAreaCode()).isEqualTo("A00001");
        assertThat(response.restStopName()).isEqualTo("서울만남(부산)휴게소");
        assertThat(response.routeName()).isEqualTo("경부선");
        assertThat(response.xValue()).isEqualTo("127.042514");
        assertThat(response.yValue()).isEqualTo("37.459939");
        assertThat(response.address()).isEqualTo("경기 성남시");
        assertThat(response.convenience()).isEqualTo("수유실|쉼터");
        assertThat(response.maintenanceYn()).isEqualTo("O");
        assertThat(response.truckSaYn()).isEqualTo("X");
        assertThat(response.direction()).isEqualTo("하행");
        assertThat(response.compactCarParkingCount()).isEqualTo(15);
        assertThat(response.fullSizeCarParkingCount()).isEqualTo(27);
        assertThat(response.disabledParkingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("상세 주소가 없으면 고속도로 휴게소 정보 주소를 사용한다")
    void of_usesFallbackAddressWhenDetailAddressMissing() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        HighwayServiceAreaInfoEntity info = highwayServiceAreaInfo("10", "20", "1");

        RestStopDetailViewResponse response = RestStopDetailViewResponse.of(restStop, Optional.empty(), List.of(info));

        assertThat(response.address()).isEqualTo("대전광역시 유성구 방현동 86");
    }

    @Test
    @DisplayName("조회 데이터가 없으면 선택 상세 필드는 null이다")
    void of_returnsNullFieldsWhenOptionalDataMissing() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));

        RestStopDetailViewResponse response = RestStopDetailViewResponse.of(restStop, Optional.empty(), List.of());

        assertThat(response.address()).isNull();
        assertThat(response.convenience()).isNull();
        assertThat(response.maintenanceYn()).isNull();
        assertThat(response.truckSaYn()).isNull();
        assertThat(response.direction()).isNull();
        assertThat(response.compactCarParkingCount()).isNull();
        assertThat(response.fullSizeCarParkingCount()).isNull();
        assertThat(response.disabledParkingCount()).isNull();
    }

    private RestStopDetailEntity restStopDetail(
            String address, String convenience, String maintenanceYn, String truckSaYn) {
        RestStopDetailItem item = restStopDetailItem("A00001", "서울만남(부산)휴게소");
        ReflectionTestUtils.setField(item, "svarAddr", address);
        ReflectionTestUtils.setField(item, "convenience", convenience);
        ReflectionTestUtils.setField(item, "maintenanceYn", maintenanceYn);
        ReflectionTestUtils.setField(item, "truckSaYn", truckSaYn);
        return RestStopDetailEntity.from(item);
    }

    private HighwayServiceAreaInfoEntity highwayServiceAreaInfo(
            String compactCarParkingCount, String fullSizeCarParkingCount, String disabledParkingCount) {
        HighwayServiceAreaInfoItem item = highwayServiceAreaInfoItem("000001", "서울만남주유소");
        ReflectionTestUtils.setField(item, "businessFacilityCode", "A00001");
        ReflectionTestUtils.setField(item, "compactCarParkingCount", compactCarParkingCount);
        ReflectionTestUtils.setField(item, "fullSizeCarParkingCount", fullSizeCarParkingCount);
        ReflectionTestUtils.setField(item, "disabledParkingCount", disabledParkingCount);
        return HighwayServiceAreaInfoEntity.from(item);
    }
}
