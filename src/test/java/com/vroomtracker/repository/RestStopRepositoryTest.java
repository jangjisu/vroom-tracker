package com.vroomtracker.repository;

import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.vroomtracker.domain.RestStopEntity;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RestStopRepositoryTest {

    @Autowired
    private RestStopRepository restStopRepository;

    @Test
    @DisplayName("휴게소 entity를 저장하고 조회한다")
    void saveAndFindAll_returnsSavedRestStop() {
        restStopRepository.save(RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소")));

        List<RestStopEntity> restStops = restStopRepository.findAll();

        assertThat(restStops).hasSize(1);
        assertThat(restStops.get(0).getUnitCode()).isEqualTo("001");
        assertThat(restStops.get(0).getUnitName()).isEqualTo("서울만남(부산)휴게소");
    }
}
