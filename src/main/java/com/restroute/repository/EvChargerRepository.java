package com.restroute.repository;

import com.restroute.domain.EvChargerEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvChargerRepository extends JpaRepository<EvChargerEntity, Long> {

    List<EvChargerEntity> findAllByDelYn(String delYn);

    List<EvChargerEntity> findAllByStatIdIn(Collection<String> statIds);

    List<EvChargerEntity> findAllByStatIdInAndDelYn(Collection<String> statIds, String delYn);
}
