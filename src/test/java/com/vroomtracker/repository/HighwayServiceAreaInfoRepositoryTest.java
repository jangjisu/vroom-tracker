package com.vroomtracker.repository;

import static com.vroomtracker.support.RestStopTestFixtures.highwayServiceAreaInfoItem;
import static org.assertj.core.api.Assertions.assertThat;

import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class HighwayServiceAreaInfoRepositoryTest {

    @Autowired
    private HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;

    @Test
    @DisplayName("고속도로 휴게소 정보 entity를 저장하고 조회한다")
    void saveAndFindAll_returnsSavedHighwayServiceAreaInfo() {
        highwayServiceAreaInfoRepository.save(
                HighwayServiceAreaInfoEntity.from(highwayServiceAreaInfoItem("000561", "북대전(논산)졸음쉼터")));

        List<HighwayServiceAreaInfoEntity> infos = highwayServiceAreaInfoRepository.findAll();

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0).getServiceAreaCode()).isEqualTo("000561");
        assertThat(infos.get(0).getServiceAreaName()).isEqualTo("북대전(논산)졸음쉼터");
    }
}
