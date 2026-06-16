package com.vroomtracker.service;

import static com.vroomtracker.support.RestStopTestFixtures.highwayServiceAreaInfoItem;
import static com.vroomtracker.support.RestStopTestFixtures.restOilItem;
import static com.vroomtracker.support.RestStopTestFixtures.restOilPriceItem;
import static com.vroomtracker.support.RestStopTestFixtures.restStopDetailItem;
import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vroomtracker.client.response.HighwayServiceAreaInfoItem;
import com.vroomtracker.client.response.RestBestfoodItem;
import com.vroomtracker.client.response.RestStopDetailItem;
import com.vroomtracker.controller.response.RestStopDetailViewResponse;
import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import com.vroomtracker.domain.RestFoodEntity;
import com.vroomtracker.domain.RestOilEntity;
import com.vroomtracker.domain.RestOilPriceEntity;
import com.vroomtracker.domain.RestStopDetailEntity;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.HighwayServiceAreaInfoRepository;
import com.vroomtracker.repository.RestFoodRepository;
import com.vroomtracker.repository.RestOilPriceRepository;
import com.vroomtracker.repository.RestOilRepository;
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

    @Mock
    private RestOilRepository restOilRepository;

    @Mock
    private RestOilPriceRepository restOilPriceRepository;

    @Mock
    private RestFoodRepository restFoodRepository;

    private RestStopQueryService restStopQueryService;

    @BeforeEach
    void setUp() {
        restStopQueryService = new RestStopQueryService(
                restStopRepository,
                restStopDetailRepository,
                highwayServiceAreaInfoRepository,
                restOilRepository,
                restOilPriceRepository,
                restFoodRepository);
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
    @DisplayName("serviceAreaCode 기준으로 조회한 휴게소 상세 응답을 반환한다")
    void findDetailByServiceAreaCode_returnsComposedDetail() throws Exception {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        RestStopDetailItem detailItem = restStopDetailItem("A00001", "서울만남(부산)휴게소");
        ReflectionTestUtils.setField(detailItem, "svarAddr", "경기 성남시");
        ReflectionTestUtils.setField(detailItem, "convenience", "수유실|쉼터");
        ReflectionTestUtils.setField(detailItem, "maintenanceYn", "O");
        RestStopDetailEntity detail = RestStopDetailEntity.from(detailItem);

        HighwayServiceAreaInfoEntity firstInfo = highwayServiceAreaInfo("A00001", "10", "20", "1");
        HighwayServiceAreaInfoEntity secondInfo = highwayServiceAreaInfo("A00001", "5", "7", "");
        RestOilEntity firstConvenience = restOilConvenience("00:00", "24:00", "쉼터", "고객쉼터");
        RestOilEntity secondConvenience = restOilConvenience("08:00", "20:00", "세차장", null);
        RestOilPriceEntity oilPrice = RestOilPriceEntity.from(restOilPriceItem("000002", "서울만남(부산)주유소"));

        when(restStopRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(restStop));
        when(restStopDetailRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(detail));
        when(highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode("A00001"))
                .thenReturn(List.of(firstInfo, secondInfo));
        when(restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc("0010", "서울만남(부산)"))
                .thenReturn(List.of(firstConvenience, secondConvenience));
        when(restOilPriceRepository.findByServiceAreaCode2("000002")).thenReturn(Optional.of(oilPrice));
        when(restFoodRepository.findAllByStdRestCdOrderByIdAsc("000001"))
                .thenReturn(List.of(foodEntity("농심어묵우동", "Y"), foodEntity("한우국밥", "N")));

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
        assertThat(response.oilInfo().oilCompany()).isEqualTo("AD");
        assertThat(response.oilInfo().gasolinePrice()).isEqualTo("1,999원");
        assertThat(response.oilInfo().dieselPrice()).isEqualTo("1,997원");
        assertThat(response.oilInfo().lpgPrice()).isEqualTo("1,157원");
        assertThat(response.oilInfo().telNo()).isEqualTo("02-573-7430");
        assertThat(response.oilInfo().oilStationConveniences())
                .extracting("startTime", "endTime", "name", "description")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("00:00", "24:00", "쉼터", "고객쉼터"),
                        org.assertj.core.groups.Tuple.tuple("08:00", "20:00", "세차장", null));
        assertThat(response.foodMenu().menus())
                .extracting("foodName", "representative")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("농심어묵우동", true),
                        org.assertj.core.groups.Tuple.tuple("한우국밥", false));
    }

    @Test
    @DisplayName("상세 데이터가 없으면 조합 응답의 상세 필드는 null이다")
    void findDetailByServiceAreaCode_returnsNullFieldsWhenOptionalDataMissing() throws Exception {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        when(restStopRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(restStop));
        when(restStopDetailRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.empty());
        when(highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode("A00001"))
                .thenReturn(List.of());
        when(restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc("0010", "서울만남(부산)"))
                .thenReturn(List.of());
        when(restFoodRepository.findAllByStdRestCdOrderByIdAsc("000001")).thenReturn(List.of());

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
        assertThat(response.oilInfo().oilCompany()).isNull();
        assertThat(response.oilInfo().oilStationConveniences()).isEmpty();
        assertThat(response.foodMenu().menus()).isEmpty();
    }

    @Test
    @DisplayName("기준 휴게소가 없으면 상세 정보를 반환하지 않는다")
    void findDetailByServiceAreaCode_returnsEmptyWhenRestStopMissing() {
        when(restStopRepository.findByServiceAreaCode("UNKNOWN")).thenReturn(Optional.empty());

        Optional<RestStopDetailViewResponse> result = restStopQueryService.findDetailByServiceAreaCode("UNKNOWN");

        assertThat(result).isEmpty();
    }

    private RestFoodEntity foodEntity(String foodNm, String recommendYn) throws Exception {
        String json = "{\"stdRestCd\":\"000001\",\"foodNm\":\"" + foodNm + "\",\"foodCost\":\"7000\",\"recommendyn\":\""
                + recommendYn + "\"}";
        return RestFoodEntity.from(new ObjectMapper().readValue(json, RestBestfoodItem.class));
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

    private RestOilEntity restOilConvenience(String startTime, String endTime, String name, String description) {
        var item = restOilItem("000002", "서울만남(부산)주유소");
        ReflectionTestUtils.setField(item, "startTime", startTime);
        ReflectionTestUtils.setField(item, "endTime", endTime);
        ReflectionTestUtils.setField(item, "convenienceName", name);
        ReflectionTestUtils.setField(item, "convenienceDescription", description);
        return RestOilEntity.from(item);
    }
}
