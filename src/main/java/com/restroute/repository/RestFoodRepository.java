package com.restroute.repository;

import com.restroute.domain.RestFoodEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestFoodRepository extends JpaRepository<RestFoodEntity, Long> {

    List<RestFoodEntity> findAllByStdRestCdOrderByIdAsc(String stdRestCd);

    List<RestFoodEntity> findAllByRestStopServiceAreaCodeOrderByIdAsc(String restStopServiceAreaCode);
}
