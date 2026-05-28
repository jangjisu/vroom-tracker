package com.vroomtracker.service;

import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.RestStopRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestStopQueryServiceTest {

    @Mock
    private RestStopRepository restStopRepository;

    private RestStopQueryService restStopQueryService;

    @BeforeEach
    void setUp() {
        restStopQueryService = new RestStopQueryService(restStopRepository);
    }

    @Test
    @DisplayName("저장된 휴게소 목록을 조회한다")
    void findAll_returnsSavedRestStops() {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop));

        List<RestStopEntity> restStops = restStopQueryService.findAll();

        assertThat(restStops).containsExactly(restStop);
    }
}
