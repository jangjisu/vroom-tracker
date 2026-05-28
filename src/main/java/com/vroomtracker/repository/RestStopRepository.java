package com.vroomtracker.repository;

import com.vroomtracker.domain.RestStopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestStopRepository extends JpaRepository<RestStopEntity, Long> {}
