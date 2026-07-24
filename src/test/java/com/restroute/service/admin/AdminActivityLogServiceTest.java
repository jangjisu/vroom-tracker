package com.restroute.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restroute.domain.AdminActivityLogEntity;
import com.restroute.repository.AdminActivityLogRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AdminActivityLogServiceTest {

    @Mock
    private AdminActivityLogRepository adminActivityLogRepository;

    @Mock
    private Authentication authentication;

    private AdminActivityLogService adminActivityLogService;

    @BeforeEach
    void setUp() {
        adminActivityLogService = new AdminActivityLogService(adminActivityLogRepository);
    }

    private AdminActivityLogEntity captureSavedEntity() {
        ArgumentCaptor<AdminActivityLogEntity> captor = ArgumentCaptor.forClass(AdminActivityLogEntity.class);
        verify(adminActivityLogRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("상품 판매순위 업로드를 기록한다")
    void logProductSalesUpload_savesEntryWithFileName() {
        when(authentication.getName()).thenReturn("admin");
        adminActivityLogService.logProductSalesUpload(authentication, "product.csv");

        AdminActivityLogEntity saved = captureSavedEntity();
        assertThat(saved.getActor()).isEqualTo("admin");
        assertThat(saved.getMessage()).isEqualTo("상품 판매순위 CSV(product.csv)를 업로드했습니다.");
    }

    @Test
    @DisplayName("매장 판매순위 업로드를 기록한다")
    void logStoreSalesUpload_savesEntryWithFileName() {
        when(authentication.getName()).thenReturn("admin");
        adminActivityLogService.logStoreSalesUpload(authentication, "store.csv");

        AdminActivityLogEntity saved = captureSavedEntity();
        assertThat(saved.getActor()).isEqualTo("admin");
        assertThat(saved.getMessage()).isEqualTo("매장 판매순위 CSV(store.csv)를 업로드했습니다.");
    }

    @Test
    @DisplayName("전체 휴게소명 매핑 실행을 기록한다")
    void logBackfill_savesFixedMessage() {
        when(authentication.getName()).thenReturn("admin");
        adminActivityLogService.logBackfill(authentication);

        AdminActivityLogEntity saved = captureSavedEntity();
        assertThat(saved.getActor()).isEqualTo("admin");
        assertThat(saved.getMessage()).isEqualTo("전체 휴게소명 매핑을 실행했습니다.");
    }

    @Test
    @DisplayName("휴게소 이미지 등록을 기록한다")
    void logRestStopImageSaved_savesEntryWithServiceAreaCode() {
        when(authentication.getName()).thenReturn("admin");
        adminActivityLogService.logRestStopImageSaved(authentication, "A00001");

        AdminActivityLogEntity saved = captureSavedEntity();
        assertThat(saved.getMessage()).isEqualTo("휴게소(A00001) 이미지를 등록했습니다.");
    }

    @Test
    @DisplayName("휴게소 이미지 삭제를 기록한다")
    void logRestStopImageDeleted_savesEntryWithServiceAreaCode() {
        when(authentication.getName()).thenReturn("admin");
        adminActivityLogService.logRestStopImageDeleted(authentication, "A00001");

        AdminActivityLogEntity saved = captureSavedEntity();
        assertThat(saved.getMessage()).isEqualTo("휴게소(A00001) 이미지를 삭제했습니다.");
    }

    @Test
    @DisplayName("휴게소 정보 수정을 기록한다")
    void logRestStopEdited_savesEntryWithUnitName() {
        when(authentication.getName()).thenReturn("admin");
        adminActivityLogService.logRestStopEdited(authentication, "서울만남(부산)휴게소");

        AdminActivityLogEntity saved = captureSavedEntity();
        assertThat(saved.getMessage()).isEqualTo("서울만남(부산)휴게소 정보를 수정했습니다.");
    }

    @Test
    @DisplayName("동기화 잠금 해제를 기록한다")
    void logRestStopOverrideCleared_savesEntryWithUnitName() {
        when(authentication.getName()).thenReturn("admin");
        adminActivityLogService.logRestStopOverrideCleared(authentication, "서울만남(부산)휴게소");

        AdminActivityLogEntity saved = captureSavedEntity();
        assertThat(saved.getMessage()).isEqualTo("서울만남(부산)휴게소의 동기화 잠금을 해제했습니다.");
    }

    @Test
    @DisplayName("최근 활동 로그를 레포지토리에서 그대로 반환한다")
    void findRecent_delegatesToRepository() {
        AdminActivityLogEntity entity = AdminActivityLogEntity.of("admin", "메시지", java.time.LocalDateTime.now());
        when(adminActivityLogRepository.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of(entity));

        List<AdminActivityLogEntity> results = adminActivityLogService.findRecent();

        assertThat(results).containsExactly(entity);
    }
}
