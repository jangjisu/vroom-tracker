package com.restroute.repository;

import com.restroute.domain.AdminActivityLogEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLogEntity, Long> {

    List<AdminActivityLogEntity> findTop50ByOrderByCreatedAtDesc();
}
