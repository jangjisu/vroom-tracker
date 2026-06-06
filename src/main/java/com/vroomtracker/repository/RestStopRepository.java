package com.vroomtracker.repository;

import com.vroomtracker.domain.RestStopEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestStopRepository extends JpaRepository<RestStopEntity, Long> {

    Optional<RestStopEntity> findByServiceAreaCode(String serviceAreaCode);
}
