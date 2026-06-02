package com.vroomtracker.repository;

import com.vroomtracker.domain.RestStopDetailEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestStopDetailRepository extends JpaRepository<RestStopDetailEntity, Long> {

    List<RestStopDetailEntity> findAllByServiceAreaCode(String serviceAreaCode);
}
