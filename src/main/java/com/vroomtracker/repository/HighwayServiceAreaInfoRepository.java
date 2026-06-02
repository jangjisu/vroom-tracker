package com.vroomtracker.repository;

import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HighwayServiceAreaInfoRepository extends JpaRepository<HighwayServiceAreaInfoEntity, Long> {

    List<HighwayServiceAreaInfoEntity> findAllByBusinessFacilityCode(String businessFacilityCode);
}
