package com.restroute.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.response.RestBestfoodItem;
import com.restroute.domain.RestFoodEntity;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RestFoodRepositoryTest {

    @Autowired
    private RestFoodRepository restFoodRepository;

    @Test
    @DisplayName("휴게소 코드 기준으로 음식 메뉴 여러 행을 등록 순서대로 조회한다")
    void findAllByStdRestCdOrderByIdAsc_returnsRowsInInsertionOrder() throws Exception {
        RestFoodEntity first = foodEntity("000001", "농심어묵우동");
        RestFoodEntity second = foodEntity("000001", "한우국밥");
        RestFoodEntity other = foodEntity("000099", "돈까스");
        restFoodRepository.saveAll(List.of(first, second, other));

        List<RestFoodEntity> result = restFoodRepository.findAllByStdRestCdOrderByIdAsc("000001");

        assertThat(result).containsExactly(first, second);
    }

    @Test
    @DisplayName("음식 메뉴가 없는 휴게소 코드는 빈 리스트를 반환한다")
    void findAllByStdRestCdOrderByIdAsc_returnsEmptyWhenNoMatch() throws Exception {
        restFoodRepository.save(foodEntity("000001", "농심어묵우동"));

        List<RestFoodEntity> result = restFoodRepository.findAllByStdRestCdOrderByIdAsc("999999");

        assertThat(result).isEmpty();
    }

    private RestFoodEntity foodEntity(String stdRestCd, String foodNm) throws Exception {
        String json = """
                {"stdRestCd":"%s","foodNm":"%s","foodCost":"7000","recommendyn":"N"}
                """.formatted(stdRestCd, foodNm);
        RestBestfoodItem item = new ObjectMapper().readValue(json, RestBestfoodItem.class);
        return RestFoodEntity.from(item);
    }
}
