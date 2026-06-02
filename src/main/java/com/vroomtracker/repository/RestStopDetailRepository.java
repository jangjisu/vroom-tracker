package com.vroomtracker.repository;

import com.vroomtracker.domain.RestStopDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestStopDetailRepository extends JpaRepository<RestStopDetailEntity, Long> {

    Optional<RestStopDetailEntity> findByServiceAreaCode(String serviceAreaCode);
}
