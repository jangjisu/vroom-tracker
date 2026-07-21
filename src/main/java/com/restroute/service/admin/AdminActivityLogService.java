package com.restroute.service.admin;

import com.restroute.domain.AdminActivityLogEntity;
import com.restroute.repository.AdminActivityLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminActivityLogService {

    private static final String PRODUCT_SALES_UPLOAD_MESSAGE = "상품 판매순위 CSV(%s)를 업로드했습니다.";
    private static final String STORE_SALES_UPLOAD_MESSAGE = "매장 판매순위 CSV(%s)를 업로드했습니다.";
    private static final String BACKFILL_MESSAGE = "전체 휴게소명 매핑을 실행했습니다.";
    private static final String IMAGE_SAVED_MESSAGE = "휴게소(%s) 이미지를 등록했습니다.";
    private static final String IMAGE_DELETED_MESSAGE = "휴게소(%s) 이미지를 삭제했습니다.";
    private static final String EDITED_MESSAGE = "%s 정보를 수정했습니다.";
    private static final String OVERRIDE_CLEARED_MESSAGE = "%s의 동기화 잠금을 해제했습니다.";

    private final AdminActivityLogRepository adminActivityLogRepository;

    @Transactional
    public void logProductSalesUpload(Authentication authentication, String fileName) {
        record(authentication, String.format(PRODUCT_SALES_UPLOAD_MESSAGE, fileName));
    }

    @Transactional
    public void logStoreSalesUpload(Authentication authentication, String fileName) {
        record(authentication, String.format(STORE_SALES_UPLOAD_MESSAGE, fileName));
    }

    @Transactional
    public void logBackfill(Authentication authentication) {
        record(authentication, BACKFILL_MESSAGE);
    }

    @Transactional
    public void logRestStopImageSaved(Authentication authentication, String serviceAreaCode) {
        record(authentication, String.format(IMAGE_SAVED_MESSAGE, serviceAreaCode));
    }

    @Transactional
    public void logRestStopImageDeleted(Authentication authentication, String serviceAreaCode) {
        record(authentication, String.format(IMAGE_DELETED_MESSAGE, serviceAreaCode));
    }

    @Transactional
    public void logRestStopEdited(Authentication authentication, String unitName) {
        record(authentication, String.format(EDITED_MESSAGE, unitName));
    }

    @Transactional
    public void logRestStopOverrideCleared(Authentication authentication, String unitName) {
        record(authentication, String.format(OVERRIDE_CLEARED_MESSAGE, unitName));
    }

    @Transactional(readOnly = true)
    public List<AdminActivityLogEntity> findRecent() {
        return adminActivityLogRepository.findTop50ByOrderByCreatedAtDesc();
    }

    private void record(Authentication authentication, String message) {
        adminActivityLogRepository.save(
                AdminActivityLogEntity.of(authentication.getName(), message, LocalDateTime.now()));
    }
}
