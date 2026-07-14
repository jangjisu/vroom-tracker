package com.restroute.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restroute.domain.RestStopStoreSalesRankEntity;
import com.restroute.service.salesranking.SalesRankingStoreRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RestStopStoreSalesRankRepositoryTest {

    @Autowired
    private RestStopStoreSalesRankRepository repository;

    @Test
    void savesStoreSalesRank() {
        RestStopStoreSalesRankEntity entity = RestStopStoreSalesRankEntity.from(
                new SalesRankingStoreRow("2026-06", "1", "1", "S000001", "휴게소", "M001", "매장"));

        RestStopStoreSalesRankEntity saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();
    }
}
