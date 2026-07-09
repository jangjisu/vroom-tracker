package com.restroute.repository;

import com.restroute.domain.RestStopDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestStopDetailRepository extends JpaRepository<RestStopDetailEntity, Long> {

    Optional<RestStopDetailEntity> findByServiceAreaCode(String serviceAreaCode);

    Optional<RestStopDetailEntity> findByRestStopServiceAreaCode(String restStopServiceAreaCode);
}
