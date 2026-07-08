package com.restroute.service;

import static com.restroute.support.RestStopTestFixtures.restStopDetailItem;
import static com.restroute.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restroute.controller.response.RestStopBasicInfoResponse;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestStopRepository;
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
class RestStopBasicInfoQueryServiceTest {

    @Mock
    private RestStopRepository restStopRepository;

    @Mock
    private RestStopRelatedInfoQueryService restStopRelatedInfoQueryService;

    private RestStopBasicInfoQueryService restStopBasicInfoQueryService;

    @BeforeEach
    void setUp() {
        restStopBasicInfoQueryService =
                new RestStopBasicInfoQueryService(restStopRepository, restStopRelatedInfoQueryService);
    }

    @Test
    @DisplayName("휴게소 기준 기본정보를 조회한다")
    void findByServiceAreaCode_returnsBasicInfo() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        RestStopDetailEntity detail = detail("경기 성남시", "02-573-7430", "투썸플레이스");
        when(restStopRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(restStop));
        when(restStopRelatedInfoQueryService.findByRestStop(restStop))
                .thenReturn(RestStopRelatedInfo.of(
                        Optional.of(detail), List.of(), List.of(), Optional.empty(), Optional.empty(), List.of()));

        Optional<RestStopBasicInfoResponse> result = restStopBasicInfoQueryService.findByServiceAreaCode("A00001");

        assertThat(result).isPresent();
        RestStopBasicInfoResponse response = result.get();
        assertThat(response.serviceAreaCode()).isEqualTo("A00001");
        assertThat(response.unitCode()).isEqualTo("001");
        assertThat(response.unitName()).isEqualTo("서울만남(부산)휴게소");
        assertThat(response.routeNo()).isEqualTo("0010");
        assertThat(response.routeName()).isEqualTo("경부선");
        assertThat(response.xValue()).isEqualTo("127.042514");
        assertThat(response.yValue()).isEqualTo("37.459939");
        assertThat(response.stdRestCd()).isEqualTo("000001");
        assertThat(response.address()).isEqualTo("경기 성남시");
        assertThat(response.telNo()).isEqualTo("02-573-7430");
        assertThat(response.brand()).isEqualTo("투썸플레이스");
    }

    @Test
    @DisplayName("휴게소가 없으면 기본정보가 없다")
    void findByServiceAreaCode_returnsEmptyWhenRestStopMissing() {
        when(restStopRepository.findByServiceAreaCode("UNKNOWN")).thenReturn(Optional.empty());

        Optional<RestStopBasicInfoResponse> result = restStopBasicInfoQueryService.findByServiceAreaCode("UNKNOWN");

        assertThat(result).isEmpty();
        verify(restStopRelatedInfoQueryService, never()).findByRestStop(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("상세 데이터가 없으면 rest_stop 기준 기본정보와 null 상세 필드를 반환한다")
    void findByServiceAreaCode_returnsBasicInfoWithNullDetailFields() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        when(restStopRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(restStop));
        when(restStopRelatedInfoQueryService.findByRestStop(restStop))
                .thenReturn(RestStopRelatedInfo.of(
                        Optional.empty(), List.of(), List.of(), Optional.empty(), Optional.empty(), List.of()));

        Optional<RestStopBasicInfoResponse> result = restStopBasicInfoQueryService.findByServiceAreaCode("A00001");

        assertThat(result).isPresent();
        assertThat(result.get().unitName()).isEqualTo("서울만남(부산)휴게소");
        assertThat(result.get().address()).isNull();
        assertThat(result.get().telNo()).isNull();
        assertThat(result.get().brand()).isNull();
    }

    @Test
    @DisplayName("상세 문자열이 공백이면 기본정보 상세 필드를 null로 반환한다")
    void findByServiceAreaCode_returnsNullDetailFieldsWhenTextIsBlank() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        RestStopDetailEntity detail = detail("  ", " ", "\t");
        when(restStopRepository.findByServiceAreaCode("A00001")).thenReturn(Optional.of(restStop));
        when(restStopRelatedInfoQueryService.findByRestStop(restStop))
                .thenReturn(RestStopRelatedInfo.of(
                        Optional.of(detail), List.of(), List.of(), Optional.empty(), Optional.empty(), List.of()));

        Optional<RestStopBasicInfoResponse> result = restStopBasicInfoQueryService.findByServiceAreaCode("A00001");

        assertThat(result).isPresent();
        assertThat(result.get().address()).isNull();
        assertThat(result.get().telNo()).isNull();
        assertThat(result.get().brand()).isNull();
    }

    private RestStopDetailEntity detail(String address, String telNo, String brand) {
        var item = restStopDetailItem("A00001", "서울만남(부산)휴게소");
        ReflectionTestUtils.setField(item, "svarAddr", address);
        ReflectionTestUtils.setField(item, "telNo", telNo);
        ReflectionTestUtils.setField(item, "brand", brand);
        return RestStopDetailEntity.from(item);
    }
}
