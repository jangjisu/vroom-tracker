package com.vroomtracker.repository;

import com.vroomtracker.domain.TrafficFlowEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrafficFlowRepositoryTest {

    @Autowired
    private TrafficFlowRepository trafficFlowRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findByStdYear_returnsOnlyMatchingYear")
    void findByStdYear_returnsOnlyMatchingYear() {
        em.persist(entity("2024", 14));
        em.persist(entity("2024", 15));
        em.persist(entity("2023", 14)); // 다른 연도
        em.flush();

        List<TrafficFlowEntity> result = trafficFlowRepository.findByStdYear("2024");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> "2024".equals(e.getStdYear()));
    }

    @Test
    @DisplayName("countByStdYear_returnsCorrectCount")
    void countByStdYear_returnsCorrectCount() {
        em.persist(entity("2024", 14));
        em.persist(entity("2024", 15));
        em.persist(entity("2023", 14));
        em.flush();

        assertThat(trafficFlowRepository.countByStdYear("2024")).isEqualTo(2);
        assertThat(trafficFlowRepository.countByStdYear("2023")).isEqualTo(1);
        assertThat(trafficFlowRepository.countByStdYear("2022")).isEqualTo(0);
    }

    @Test
    @DisplayName("deleteByStdYear_removesOnlyTargetYear")
    void deleteByStdYear_removesOnlyTargetYear() {
        em.persist(entity("2024", 14));
        em.persist(entity("2024", 15));
        em.persist(entity("2023", 14)); // 삭제 대상 아님
        em.flush();

        trafficFlowRepository.deleteByStdYear("2024");
        em.flush();
        em.clear();

        assertThat(trafficFlowRepository.countByStdYear("2024")).isEqualTo(0);
        assertThat(trafficFlowRepository.countByStdYear("2023")).isEqualTo(1);
    }

    @Test
    @DisplayName("findByStdYear_whenNoData_returnsEmptyList")
    void findByStdYear_whenNoData_returnsEmptyList() {
        assertThat(trafficFlowRepository.findByStdYear("2099")).isEmpty();
    }

    private TrafficFlowEntity entity(String year, int stdHour) {
        return TrafficFlowEntity.builder()
                .stdYear(year)
                .sphlDfttNm("평일")
                .sphlDfttCode("0")
                .sphlDfttScopTypeNm("당일")
                .sphlDfttScopTypeCode("0")
                .stdHour(stdHour)
                .trfl(1000L)
                .fetchedAt(LocalDateTime.now())
                .build();
    }
}
