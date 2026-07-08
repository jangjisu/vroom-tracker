package com.restroute.service;

import static com.restroute.support.RestStopTestFixtures.highwayServiceAreaInfoItem;
import static com.restroute.support.RestStopTestFixtures.restOilItem;
import static com.restroute.support.RestStopTestFixtures.restOilPriceItem;
import static com.restroute.support.RestStopTestFixtures.restStopDetailItem;
import static com.restroute.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.response.RestBestfoodItem;
import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.domain.RestFoodEntity;
import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestOilPriceEntity;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.HighwayServiceAreaInfoRepository;
import com.restroute.repository.RestFoodRepository;
import com.restroute.repository.RestOilPriceRepository;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopDetailRepository;
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
class RestStopRelatedInfoQueryServiceTest {

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

    private RestStopRelatedInfoQueryService service;

    @BeforeEach
    void setUp() {
        service = new RestStopRelatedInfoQueryService(
                restStopDetailRepository,
                highwayServiceAreaInfoRepository,
                restOilRepository,
                restOilPriceRepository,
                restFoodRepository);
    }

    @Test
    @DisplayName("rest_stop 기준 연결 키로 상세, 영업시설, 주유, 가격, 음식 정보를 조회한다")
    void findByRestStop_returnsRelatedInfo() throws Exception {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        RestStopDetailEntity detail = RestStopDetailEntity.from(restStopDetailItem("A00001", "서울만남(부산)휴게소"));
        HighwayServiceAreaInfoEntity info =
                HighwayServiceAreaInfoEntity.from(highwayServiceAreaInfoItem("000001", "서울만남주유소"));
        RestOilEntity oilConvenience = RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소"));
        RestOilPriceEntity oilPrice = RestOilPriceEntity.from(restOilPriceItem("000002", "서울만남(부산)주유소"));
        RestFoodEntity food = foodEntity("농심어묵우동");

        when(restStopDetailRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(detail));
        when(highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode("A00001"))
                .thenReturn(List.of(info));
        when(restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc("0010", "서울만남(부산)"))
                .thenReturn(List.of(oilConvenience));
        when(restOilPriceRepository.findByServiceAreaCode2("000002")).thenReturn(Optional.of(oilPrice));
        when(restFoodRepository.findAllByStdRestCdOrderByIdAsc("000001")).thenReturn(List.of(food));

        RestStopRelatedInfo relatedInfo = service.findByRestStop(restStop);

        assertThat(relatedInfo.detail()).contains(detail);
        assertThat(relatedInfo.highwayServiceAreaInfos()).containsExactly(info);
        assertThat(relatedInfo.oilStationConveniences()).containsExactly(oilConvenience);
        assertThat(relatedInfo.oilServiceAreaCode2()).contains("000002");
        assertThat(relatedInfo.oilPrice()).contains(oilPrice);
        assertThat(relatedInfo.foods()).containsExactly(food);
    }

    @Test
    @DisplayName("주유 편의시설 매핑이 없으면 주유 가격을 조회하지 않는다")
    void findByRestStop_skipsOilPriceWhenOilMappingMissing() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));

        when(restStopDetailRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.empty());
        when(highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode("A00001"))
                .thenReturn(List.of());
        when(restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc("0010", "서울만남(부산)"))
                .thenReturn(List.of());
        when(restFoodRepository.findAllByStdRestCdOrderByIdAsc("000001")).thenReturn(List.of());

        RestStopRelatedInfo relatedInfo = service.findByRestStop(restStop);

        assertThat(relatedInfo.detail()).isEmpty();
        assertThat(relatedInfo.highwayServiceAreaInfos()).isEmpty();
        assertThat(relatedInfo.oilStationConveniences()).isEmpty();
        assertThat(relatedInfo.oilServiceAreaCode2()).isEmpty();
        assertThat(relatedInfo.oilPrice()).isEmpty();
        assertThat(relatedInfo.foods()).isEmpty();
        verify(restOilPriceRepository, never()).findByServiceAreaCode2(org.mockito.ArgumentMatchers.anyString());
    }

    private RestFoodEntity foodEntity(String foodName) throws Exception {
        RestBestfoodItem item = new ObjectMapper()
                .readValue("{\"stdRestCd\":\"000001\",\"foodNm\":\"" + foodName + "\"}", RestBestfoodItem.class);
        ReflectionTestUtils.setField(item, "recommendyn", "Y");
        return RestFoodEntity.from(item);
    }
}
