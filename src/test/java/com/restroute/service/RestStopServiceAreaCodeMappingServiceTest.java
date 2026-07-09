package com.restroute.service;

import static com.restroute.support.RestStopTestFixtures.restOilItem;
import static com.restroute.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestStopServiceAreaCodeMappingServiceTest {

    @Mock
    private RestStopRepository restStopRepository;

    @Mock
    private RestOilRepository restOilRepository;

    private RestStopServiceAreaCodeMappingService service;

    @BeforeEach
    void setUp() {
        service = new RestStopServiceAreaCodeMappingService(restStopRepository, restOilRepository);
    }

    @Test
    @DisplayName("REST_STOP serviceAreaCode 원본 값을 내부 조회 키로 매핑한다")
    void mapByServiceAreaCode_returnsServiceAreaCodeLookupMap() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001"));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop));

        Map<String, String> result = service.mapByServiceAreaCode();

        assertThat(result).containsEntry("A00001", "A00001");
    }

    @Test
    @DisplayName("REST_STOP serviceAreaCode가 중복되면 첫 번째 매핑을 유지한다")
    void mapByServiceAreaCode_keepsFirstMappingWhenDuplicated() {
        RestStopEntity first = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001"));
        RestStopEntity second = RestStopEntity.from(restStopItem("002", "중복휴게소", "A00001"));
        when(restStopRepository.findAll()).thenReturn(List.of(first, second));

        Map<String, String> result = service.mapByServiceAreaCode();

        assertThat(result).containsEntry("A00001", "A00001");
    }

    @Test
    @DisplayName("REST_FOOD용 stdRestCd를 REST_STOP serviceAreaCode로 매핑한다")
    void mapByStdRestCd_returnsStdRestCdLookupMap() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001"));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop));

        Map<String, String> result = service.mapByStdRestCd();

        assertThat(result).containsEntry("000001", "A00001");
    }

    @Test
    @DisplayName("REST_FOOD용 stdRestCd가 중복되면 첫 번째 매핑을 유지한다")
    void mapByStdRestCd_keepsFirstMappingWhenDuplicated() {
        RestStopEntity first = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001"));
        RestStopEntity second = RestStopEntity.from(restStopItem("002", "중복휴게소", "A00002"));
        when(restStopRepository.findAll()).thenReturn(List.of(first, second));

        Map<String, String> result = service.mapByStdRestCd();

        assertThat(result).containsEntry("000001", "A00001");
    }

    @Test
    @DisplayName("REST_OIL용 routeCode와 정규화 주유소명을 REST_STOP serviceAreaCode로 매핑한다")
    void mapByOilRestStopKey_returnsOilLookupMap() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001"));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop));

        Map<String, String> result = service.mapByOilRestStopKey();

        assertThat(result)
                .containsEntry(RestStopServiceAreaCodeMappingService.oilRestStopKey("0010", "서울만남(부산)"), "A00001");
    }

    @Test
    @DisplayName("REST_OIL용 routeCode와 정규화 주유소명이 중복되면 첫 번째 매핑을 유지한다")
    void mapByOilRestStopKey_keepsFirstMappingWhenDuplicated() {
        RestStopEntity first = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소", "A00001"));
        RestStopEntity second = RestStopEntity.from(restStopItem("002", "서울만남(부산)휴게소", "A00002"));
        when(restStopRepository.findAll()).thenReturn(List.of(first, second));

        Map<String, String> result = service.mapByOilRestStopKey();

        assertThat(result)
                .containsEntry(RestStopServiceAreaCodeMappingService.oilRestStopKey("0010", "서울만남(부산)"), "A00001");
    }

    @Test
    @DisplayName("REST_OIL_PRICE용 주유소 코드를 REST_OIL에 저장된 내부 조회 키로 매핑한다")
    void mapByOilStandardRestCode_returnsOilPriceLookupMap() {
        RestOilEntity oil = RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소"));
        oil.updateRestStopServiceAreaCode("A00001");
        when(restOilRepository.findAll()).thenReturn(List.of(oil));

        Map<String, String> result = service.mapByOilStandardRestCode();

        assertThat(result).containsEntry("000002", "A00001");
    }

    @Test
    @DisplayName("REST_OIL_PRICE용 주유소 코드가 중복되면 첫 번째 매핑을 유지한다")
    void mapByOilStandardRestCode_keepsFirstMappingWhenDuplicated() {
        RestOilEntity first = RestOilEntity.from(restOilItem("000002", "서울만남(부산)주유소"));
        RestOilEntity second = RestOilEntity.from(restOilItem("000002", "중복주유소"));
        first.updateRestStopServiceAreaCode("A00001");
        second.updateRestStopServiceAreaCode("A00002");
        when(restOilRepository.findAll()).thenReturn(List.of(first, second));

        Map<String, String> result = service.mapByOilStandardRestCode();

        assertThat(result).containsEntry("000002", "A00001");
    }
}
