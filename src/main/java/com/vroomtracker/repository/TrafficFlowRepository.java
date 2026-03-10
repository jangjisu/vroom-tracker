package com.vroomtracker.repository;

import com.vroomtracker.domain.TrafficFlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TrafficFlowRepository extends JpaRepository<TrafficFlowEntity, Long> {

    List<TrafficFlowEntity> findByStdYear(String stdYear);

    long countByStdYear(String stdYear);

    @Transactional
    @Modifying
    @Query("DELETE FROM TrafficFlowEntity t WHERE t.stdYear = :stdYear")
    void deleteByStdYear(@Param("stdYear") String stdYear);
}
