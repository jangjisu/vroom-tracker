package com.restroute.repository;

import com.restroute.domain.NationalOilPriceEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NationalOilPriceRepository extends JpaRepository<NationalOilPriceEntity, Long> {

    List<NationalOilPriceEntity> findAllByTradeDate(LocalDate tradeDate);

    void deleteAllByTradeDate(LocalDate tradeDate);
}
