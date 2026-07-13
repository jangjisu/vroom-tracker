package com.restroute.service;

import static com.restroute.support.RestStopTestFixtures.highwayServiceAreaInfoItem;
import static com.restroute.support.RestStopTestFixtures.restOilItem;
import static com.restroute.support.RestStopTestFixtures.restOilPriceItem;
import static com.restroute.support.RestStopTestFixtures.restStopDetailItem;
import static com.restroute.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.response.RestBestfoodItem;
import com.restroute.client.response.RestOilItem;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.domain.RestFoodEntity;
import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestOilPriceEntity;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.EvChargerStationMappingRepository;
import com.restroute.repository.HighwayServiceAreaInfoRepository;
import com.restroute.repository.RestFoodRepository;
import com.restroute.repository.RestOilPriceRepository;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopDetailRepository;
import com.restroute.repository.RestStopRepository;
import com.restroute.service.evcharger.EvChargerStationMappingMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import({RestStopServiceAreaCodeBackfillService.class, EvChargerStationMappingMapper.class})
class RestStopServiceAreaCodeBackfillServiceTest {

    @Autowired
    private RestStopServiceAreaCodeBackfillService backfillService;

    @Autowired
    private RestStopRepository restStopRepository;

    @Autowired
    private RestStopDetailRepository restStopDetailRepository;

    @Autowired
    private HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;

    @Autowired
    private RestFoodRepository restFoodRepository;

    @Autowired
    private RestOilRepository restOilRepository;

    @Autowired
    private RestOilPriceRepository restOilPriceRepository;

    @Autowired
    private EvChargerStationMappingRepository evChargerStationMappingRepository;

    @Test
    @DisplayName("기존 휴게소 관련 row에 rest_stop_service_area_code를 연결 규칙 순서대로 채운다")
    void backfill_mapsExistingRowsByRestStopServiceAreaCode() throws Exception {
        restStopRepository.save(RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001")));
        restStopDetailRepository.save(RestStopDetailEntity.from(restStopDetailItem("A00001", "서울만남(부산)휴게소")));
        HighwayServiceAreaInfoEntity highwayServiceAreaInfo =
                HighwayServiceAreaInfoEntity.from(highwayServiceAreaInfoItem("SA001", "서울만남(부산)휴게소"));
        highwayServiceAreaInfo.updateRestStopServiceAreaCode("stale");
        ReflectionTestUtils.setField(highwayServiceAreaInfo, "businessFacilityCode", "A00001");
        highwayServiceAreaInfoRepository.save(highwayServiceAreaInfo);
        restFoodRepository.save(foodEntity("000001", "한우국밥"));
        restOilRepository.save(RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소")));
        restOilPriceRepository.save(RestOilPriceEntity.from(restOilPriceItem("000002", "서울만남(부산)주유소")));

        Map<String, Integer> result = backfillService.backfill();

        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_STOP_DETAIL_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.HIGHWAY_SERVICE_AREA_INFO_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_FOOD_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_OIL_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_OIL_PRICE_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(restStopDetailRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
        assertThat(highwayServiceAreaInfoRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
        assertThat(restFoodRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
        assertThat(restOilRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
        assertThat(restOilPriceRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
    }

    @Test
    @DisplayName("매핑되지 않는 기존 row는 삭제하지 않고 조회 키를 null로 유지한다")
    void backfill_keepsUnmatchedRowsWithNullLookupKey() throws Exception {
        restStopRepository.save(RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001")));
        restStopDetailRepository.save(RestStopDetailEntity.from(restStopDetailItem("A99999", "미매칭휴게소")));
        HighwayServiceAreaInfoEntity highwayServiceAreaInfo =
                HighwayServiceAreaInfoEntity.from(highwayServiceAreaInfoItem("SA999", "미매칭휴게소"));
        ReflectionTestUtils.setField(highwayServiceAreaInfo, "businessFacilityCode", "A99999");
        highwayServiceAreaInfoRepository.save(highwayServiceAreaInfo);
        restFoodRepository.save(foodEntity("999999", "미매칭메뉴"));
        RestOilItem unmatchedOil = restOilItem("999999", "미매칭주유소");
        ReflectionTestUtils.setField(unmatchedOil, "routeCode", "9999");
        restOilRepository.save(RestOilEntity.from(unmatchedOil));
        restOilPriceRepository.save(RestOilPriceEntity.from(restOilPriceItem("999999", "미매칭주유소")));

        Map<String, Integer> result = backfillService.backfill();

        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_STOP_DETAIL_MAPPED_COUNT))
                .isZero();
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.HIGHWAY_SERVICE_AREA_INFO_MAPPED_COUNT))
                .isZero();
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_FOOD_MAPPED_COUNT))
                .isZero();
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_OIL_MAPPED_COUNT))
                .isZero();
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_OIL_PRICE_MAPPED_COUNT))
                .isZero();
        assertThat(restStopDetailRepository.count()).isEqualTo(1);
        assertThat(restStopDetailRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isNull();
        assertThat(highwayServiceAreaInfoRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isNull();
        assertThat(restFoodRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isNull();
        assertThat(restOilRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isNull();
        assertThat(restOilPriceRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isNull();
    }

    @Test
    @DisplayName("주유 가격 backfill은 주유소 편의시설에 먼저 채워진 조회 키를 사용한다")
    void backfill_mapsOilPriceFromRestOilLookupKey() {
        restStopRepository.save(RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001")));
        restOilRepository.save(RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소")));
        restOilPriceRepository.save(RestOilPriceEntity.from(restOilPriceItem("000002", "서울만남(부산)주유소")));

        backfillService.backfill();

        assertThat(restOilRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
        assertThat(restOilPriceRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
    }

    @Test
    @DisplayName("조회 키 기준 repository 메서드는 기존 조회 전환 전에도 사용할 수 있다")
    void repositories_findByRestStopServiceAreaCode() throws Exception {
        restStopRepository.save(RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001")));
        restStopDetailRepository.save(RestStopDetailEntity.from(restStopDetailItem("A00001", "서울만남(부산)휴게소")));
        HighwayServiceAreaInfoEntity highwayServiceAreaInfo =
                HighwayServiceAreaInfoEntity.from(highwayServiceAreaInfoItem("SA001", "서울만남(부산)휴게소"));
        ReflectionTestUtils.setField(highwayServiceAreaInfo, "businessFacilityCode", "A00001");
        highwayServiceAreaInfoRepository.save(highwayServiceAreaInfo);
        restFoodRepository.save(foodEntity("000001", "한우국밥"));
        restOilRepository.save(RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소")));
        restOilPriceRepository.save(RestOilPriceEntity.from(restOilPriceItem("000002", "서울만남(부산)주유소")));
        backfillService.backfill();

        assertThat(restStopDetailRepository.findByRestStopServiceAreaCode("A00001"))
                .isPresent();
        assertThat(highwayServiceAreaInfoRepository.findAllByRestStopServiceAreaCode("A00001"))
                .hasSize(1);
        assertThat(restFoodRepository.findAllByRestStopServiceAreaCodeOrderByIdAsc("A00001"))
                .hasSize(1);
        assertThat(restOilRepository.findAllByRestStopServiceAreaCodeOrderByIdAsc("A00001"))
                .hasSize(1);
        assertThat(restOilPriceRepository.findAllByRestStopServiceAreaCodeOrderByIdAsc("A00001"))
                .hasSize(1);
    }

    @Test
    @DisplayName("중복 매핑 후보가 있어도 첫 번째 매핑을 유지하고 backfill을 계속한다")
    void backfill_keepsFirstMappingWhenCandidatesAreDuplicated() throws Exception {
        restStopRepository.saveAll(List.of(
                RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001")),
                RestStopEntity.from(restStopItem("002", "서울만남(부산)휴게소", "A00001"))));
        restStopDetailRepository.save(RestStopDetailEntity.from(restStopDetailItem("A00001", "서울만남(부산)휴게소")));
        restFoodRepository.save(foodEntity("000001", "한우국밥"));
        restOilRepository.saveAll(List.of(
                RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소", "07")),
                RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소", "08"))));
        restOilPriceRepository.save(RestOilPriceEntity.from(restOilPriceItem("000002", "서울만남(부산)주유소")));

        Map<String, Integer> result = backfillService.backfill();

        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_STOP_DETAIL_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_FOOD_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_OIL_MAPPED_COUNT))
                .isEqualTo(2);
        assertThat(result.get(RestStopServiceAreaCodeBackfillService.REST_OIL_PRICE_MAPPED_COUNT))
                .isEqualTo(1);
        assertThat(restOilPriceRepository.findAll().get(0).getRestStopServiceAreaCode())
                .isEqualTo("A00001");
    }

    @Test
    @DisplayName("중앙 backfill은 EV 매핑 클래스의 결과를 저장한다")
    void backfill_replacesEvMappingsWithMapperResult() {
        EvChargerStationMappingEntity existing = EvChargerStationMappingEntity.of("ME1");
        existing.updateMatch("A00001", 100.0, "COORDINATE");
        evChargerStationMappingRepository.save(existing);

        Map<String, Integer> result = backfillService.backfill();

        assertThat(result.get(RestStopServiceAreaCodeBackfillService.EV_CHARGER_MAPPED_COUNT))
                .isZero();
        assertThat(evChargerStationMappingRepository.findAll()).isEmpty();
    }

    private RestFoodEntity foodEntity(String stdRestCd, String foodName) throws Exception {
        String json = """
                {"stdRestCd":"%s","foodNm":"%s","foodCost":"7000","recommendyn":"N"}
                """.formatted(stdRestCd, foodName);
        RestBestfoodItem item = new ObjectMapper().readValue(json, RestBestfoodItem.class);
        return RestFoodEntity.from(item);
    }
}
