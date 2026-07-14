package com.restroute.service;

import com.restroute.controller.response.RestStopSalesRankingResponse;
import com.restroute.domain.RestStopProductSalesRankEntity;
import com.restroute.repository.RestStopProductSalesRankRepository;
import com.restroute.repository.RestStopRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RestStopSalesRankingQueryService {

    private static final int TOP_PRODUCT_COUNT = 5;

    private final RestStopRepository restStopRepository;
    private final RestStopProductSalesRankRepository productSalesRankRepository;

    @Transactional(readOnly = true)
    public Optional<RestStopSalesRankingResponse> findByServiceAreaCode(String serviceAreaCode) {
        if (!StringUtils.hasText(serviceAreaCode)) {
            return Optional.empty();
        }

        if (restStopRepository.findByServiceAreaCode(serviceAreaCode).isEmpty()) {
            return Optional.empty();
        }

        List<RestStopProductSalesRankEntity> mappedProducts =
                productSalesRankRepository.findAllMappedProductsOrderByLatestMonth(serviceAreaCode);
        List<RestStopProductSalesRankEntity> validProducts =
                mappedProducts.stream().filter(this::hasValidRank).toList();
        if (validProducts.isEmpty()) {
            return Optional.of(RestStopSalesRankingResponse.empty());
        }

        String latestMonth = validProducts.get(0).getBaseYearMonth();
        List<RestStopProductSalesRankEntity> topProducts = validProducts.stream()
                .filter(product -> latestMonth.equals(product.getBaseYearMonth()))
                .sorted(Comparator.comparingInt(product -> Integer.parseInt(product.getRestStopRank())))
                .limit(TOP_PRODUCT_COUNT)
                .toList();
        return Optional.of(RestStopSalesRankingResponse.of(latestMonth, topProducts));
    }

    private boolean hasValidRank(RestStopProductSalesRankEntity product) {
        if (!StringUtils.hasText(product.getBaseYearMonth())
                || !StringUtils.hasText(product.getRestStopRank())
                || !StringUtils.hasText(product.getProductName())) {
            return false;
        }

        try {
            return Integer.parseInt(product.getRestStopRank()) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
