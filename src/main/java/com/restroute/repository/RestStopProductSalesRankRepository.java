package com.restroute.repository;

import com.restroute.domain.RestStopProductSalesRankEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestStopProductSalesRankRepository extends JpaRepository<RestStopProductSalesRankEntity, Long> {

    Optional<RestStopProductSalesRankEntity> findTopByOrderByBaseYearMonthDesc();
}
