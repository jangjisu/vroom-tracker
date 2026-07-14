package com.restroute.repository;

import com.restroute.domain.RestStopStoreSalesRankEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestStopStoreSalesRankRepository extends JpaRepository<RestStopStoreSalesRankEntity, Long> {

    Optional<RestStopStoreSalesRankEntity> findTopByOrderByBaseYearMonthDesc();
}
