package com.vroomtracker.service;

import static com.vroomtracker.support.RestStopTestFixtures.highwayServiceAreaInfoItem;
import static com.vroomtracker.support.RestStopTestFixtures.restStopDetailItem;
import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.vroomtracker.client.response.HighwayServiceAreaInfoItem;
import com.vroomtracker.client.response.RestStopDetailItem;
import com.vroomtracker.controller.response.RestStopDetailViewResponse;
import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import com.vroomtracker.domain.RestStopDetailEntity;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.HighwayServiceAreaInfoRepository;
import com.vroomtracker.repository.RestStopDetailRepository;
import com.vroomtracker.repository.RestStopRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RestStopQueryServiceTest {

    @Mock
    private RestStopRepository restStopRepository;

    @Mock
    private RestStopDetailRepository restStopDetailRepository;

    @Mock
    private HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;

    private RestStopQueryService restStopQueryService;

    @BeforeEach
    void setUp() {
        restStopQueryService = new RestStopQueryService(
                restStopRepository, restStopDetailRepository, highwayServiceAreaInfoRepository);
    }

    @Test
    @DisplayName("저장된 휴게소 목록을 조회한다")
    void findAll_returnsSavedRestStops() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop));

        List<RestStopEntity> restStops = restStopQueryService.findAll();

        assertThat(restStops).containsExactly(restStop);
    }

    @Test
    @DisplayName("serviceAreaCode 기준으로 휴게소 상세 정보를 조합한다")
    void findDetailByServiceAreaCode_returnsComposedDetail() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        RestStopDetailItem detailItem = restStopDetailItem("A00001", "서울만남(부산)휴게소");
        ReflectionTestUtils.setField(detailItem, "svarAddr", "경기 성남시");
        ReflectionTestUtils.setField(detailItem, "convenience", "수유실|쉼터");
        ReflectionTestUtils.setField(detailItem, "maintenanceYn", "O");
        RestStopDetailEntity detail = RestStopDetailEntity.from(detailItem);

        HighwayServiceAreaInfoEntity firstInfo = highwayServiceAreaInfo("A00001", "10", "20", "1");
        HighwayServiceAreaInfoEntity secondInfo = highwayServiceAreaInfo("A00001", "5", "7", "");

        when(restStopRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(restStop));
        when(restStopDetailRepository.findAllByServiceAreaCode("A00001")).thenReturn(List.of(detail));
        when(highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode("A00001"))
                .thenReturn(List.of(firstInfo, secondInfo));

        Optional<RestStopDetailViewResponse> result = restStopQueryService.findDetailByServiceAreaCode("A00001");

        assertThat(result).isPresent();
        RestStopDetailViewResponse response = result.get();
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
    @DisplayName("상세 데이터가 없으면 조합 응답의 상세 필드는 null이다")
    void findDetailByServiceAreaCode_returnsNullFieldsWhenOptionalDataMissing() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        when(restStopRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(restStop));
        when(restStopDetailRepository.findAllByServiceAreaCode("A00001")).thenReturn(List.of());
        when(highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode("A00001"))
                .thenReturn(List.of());

        Optional<RestStopDetailViewResponse> result = restStopQueryService.findDetailByServiceAreaCode("A00001");

        assertThat(result).isPresent();
        RestStopDetailViewResponse response = result.get();
        assertThat(response.address()).isNull();
        assertThat(response.convenience()).isNull();
        assertThat(response.maintenanceYn()).isNull();
        assertThat(response.truckSaYn()).isNull();
        assertThat(response.direction()).isNull();
        assertThat(response.compactCarParkingCount()).isNull();
        assertThat(response.fullSizeCarParkingCount()).isNull();
        assertThat(response.disabledParkingCount()).isNull();
    }

    @Test
    @DisplayName("기준 휴게소가 없으면 상세 정보를 반환하지 않는다")
    void findDetailByServiceAreaCode_returnsEmptyWhenRestStopMissing() {
        when(restStopRepository.findByServiceAreaCode("UNKNOWN")).thenReturn(Optional.empty());

        Optional<RestStopDetailViewResponse> result = restStopQueryService.findDetailByServiceAreaCode("UNKNOWN");

        assertThat(result).isEmpty();
    }

    private HighwayServiceAreaInfoEntity highwayServiceAreaInfo(
            String businessFacilityCode,
            String compactCarParkingCount,
            String fullSizeCarParkingCount,
            String disabledParkingCount) {
        HighwayServiceAreaInfoItem item = highwayServiceAreaInfoItem("000001", "서울만남주유소");
        ReflectionTestUtils.setField(item, "businessFacilityCode", businessFacilityCode);
        ReflectionTestUtils.setField(item, "compactCarParkingCount", compactCarParkingCount);
        ReflectionTestUtils.setField(item, "fullSizeCarParkingCount", fullSizeCarParkingCount);
        ReflectionTestUtils.setField(item, "disabledParkingCount", disabledParkingCount);
        return HighwayServiceAreaInfoEntity.from(item);
    }
}
