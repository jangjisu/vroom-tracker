package com.vroomtracker.repository;

import com.vroomtracker.domain.RestOilPriceEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestOilPriceRepository extends JpaRepository<RestOilPriceEntity, Long> {

    Optional<RestOilPriceEntity> findByServiceAreaCode2(String serviceAreaCode2);
}
