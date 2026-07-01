package com.restroute.repository;

import com.restroute.domain.RestOilEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestOilRepository extends JpaRepository<RestOilEntity, Long> {

    List<RestOilEntity> findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc(
            String routeCode, String normalizedStationName);
}
