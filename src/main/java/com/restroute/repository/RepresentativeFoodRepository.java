package com.restroute.repository;

import com.restroute.domain.RepresentativeFoodEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepresentativeFoodRepository extends JpaRepository<RepresentativeFoodEntity, Long> {

    List<RepresentativeFoodEntity> findAllByServiceAreaCodeIn(Collection<String> serviceAreaCodes);
}
