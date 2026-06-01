package com.vroomtracker.repository;

import static com.vroomtracker.support.RestStopTestFixtures.restStopDetailItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.vroomtracker.domain.RestStopDetailEntity;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RestStopDetailRepositoryTest {

    @Autowired
    private RestStopDetailRepository restStopDetailRepository;

    @Test
    @DisplayName("휴게소 상세 entity를 저장하고 조회한다")
    void saveAndFindAll_returnsSavedRestStopDetail() {
        restStopDetailRepository.save(RestStopDetailEntity.from(restStopDetailItem("A00078", "건천(부산)휴게소")));

        List<RestStopDetailEntity> details = restStopDetailRepository.findAll();

        assertThat(details).hasSize(1);
        assertThat(details.get(0).getServiceAreaCode()).isEqualTo("A00078");
        assertThat(details.get(0).getServiceAreaName()).isEqualTo("건천(부산)휴게소");
    }
}
