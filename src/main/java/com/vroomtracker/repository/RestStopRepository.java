package com.vroomtracker.repository;

import com.vroomtracker.domain.RestStopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RestStopRepository extends JpaRepository<RestStopEntity, Long> {

    @Query("select count(r) from RestStopEntity r where r.xValue is null or r.yValue is null")
    long countByXValueIsNullOrYValueIsNull();
}
