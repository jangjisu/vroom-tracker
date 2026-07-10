package com.restroute.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.response.RepresentativeFoodItem;
import com.restroute.domain.RepresentativeFoodEntity;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RepresentativeFoodRepositoryTest {

    @Autowired
    private RepresentativeFoodRepository representativeFoodRepository;

    @Test
    @DisplayName("후보 휴게소 코드 묶음으로 대표 음식 데이터를 일괄 조회한다")
    void findAllByServiceAreaCodeIn_returnsOnlyRequestedCodes() throws Exception {
        RepresentativeFoodEntity matched = foodEntity("A00001", "말죽거리소고기국밥");
        RepresentativeFoodEntity other = foodEntity("A00002", "한우국밥");
        representativeFoodRepository.saveAll(List.of(matched, other));

        assertThat(representativeFoodRepository.findAllByServiceAreaCodeIn(List.of("A00001")))
                .extracting(RepresentativeFoodEntity::getServiceAreaCode)
                .containsExactly("A00001");
    }

    private RepresentativeFoodEntity foodEntity(String serviceAreaCode, String menu) throws Exception {
        RepresentativeFoodItem item = new ObjectMapper()
                .readValue(
                        "{\"serviceAreaCode\":\"" + serviceAreaCode + "\",\"batchMenu\":\"" + menu
                                + "\",\"salePrice\":\"￦6,000\"}",
                        RepresentativeFoodItem.class);
        return RepresentativeFoodEntity.from(item, serviceAreaCode);
    }
}
