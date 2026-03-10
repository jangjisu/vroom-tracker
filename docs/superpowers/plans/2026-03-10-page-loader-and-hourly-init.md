# Page Loader & Hourly-Pattern Init Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 전역 페이지 로딩 오버레이 추가 + @PostConstruct 제거 후 프론트엔드에서 hourly-pattern 데이터 없을 때 init API 호출로 대체

**Architecture:** 전역 오버레이는 HTML에서 바로 렌더링되어 JS 실행 전부터 보임. app.js에서 3개 fetch를 Promise.allSettled로 묶어 모두 완료되면 오버레이 제거. hourly-pattern이 비어있으면 오버레이 제거 후 해당 섹션에서 POST /api/hourly-pattern/init 호출 후 재조회.

**Tech Stack:** Spring Boot, Feign Client, Vanilla JS (ES module), Bootstrap 5

---

## Chunk 1: 백엔드

### Task 1: Feign 전역 timeout 설정

**Files:**
- Modify: `src/main/resources/application.properties`

- [ ] `application.properties`에 Feign timeout 추가

```properties
# Feign Client global timeout
spring.cloud.openfeign.client.config.default.connectTimeout=3000
spring.cloud.openfeign.client.config.default.readTimeout=10000
```

---

### Task 2: TrafficFlowService — @PostConstruct 제거 + null guard + initIfEmpty

**Files:**
- Modify: `src/main/java/com/vroomtracker/service/TrafficFlowService.java`
- Modify: `src/test/java/com/vroomtracker/service/TrafficFlowServiceTest.java`

- [ ] `initialize()` 메서드 및 `@PostConstruct` import 제거
- [ ] `refreshByYear()` 내 엔티티 생성 전 null/blank 필드 검증 추가

```java
boolean hasInvalidItem = items.stream().anyMatch(item ->
    item.getStdHour() == null || item.getStdHour().isBlank() ||
    item.getTrfl()    == null || item.getTrfl().isBlank()
);
if (hasInvalidItem) {
    log.warn("{}년 trafficFlowByTime 응답에 유효하지 않은 항목 포함, 기존 데이터 유지", year);
    return;
}
```

- [ ] `initIfEmpty(String year)` 메서드 추가

```java
public void initIfEmpty(String year) {
    if (trafficFlowRepository.countByStdYear(year) == 0) {
        log.info("DB에 {}년 trafficFlow 데이터 없음, API 초기 적재 시작", year);
        refreshByYear(year);
    }
}
```

- [ ] `TrafficFlowServiceTest` 업데이트
  - `initialize` 관련 테스트 제거
  - `initIfEmpty_whenEmpty_callsRefresh` 테스트 추가
  - `initIfEmpty_whenNotEmpty_skipsRefresh` 테스트 추가
  - `refreshByYear_whenItemHasNullHour_keepsExistingData` 테스트 추가
  - `refreshByYear_whenItemHasNullTrfl_keepsExistingData` 테스트 추가

- [ ] `./gradlew test` 실행 후 전체 통과 확인

---

### Task 3: TrafficApiController — POST /api/hourly-pattern/init 추가

**Files:**
- Modify: `src/main/java/com/vroomtracker/controller/TrafficApiController.java`
- Modify: `src/test/java/com/vroomtracker/controller/TrafficApiControllerTest.java`

- [ ] 컨트롤러에 init 엔드포인트 추가

```java
@PostMapping("/hourly-pattern/init")
public ResponseEntity<ApiResponse<Void>> initHourlyPattern() {
    String year = String.valueOf(LocalDateTime.now().getYear());
    trafficFlowService.initIfEmpty(year);
    return ResponseEntity.ok(ApiResponse.success(null));
}
```

- [ ] `TrafficApiControllerTest`에 테스트 추가

```java
@Test
@DisplayName("POST /api/hourly-pattern/init returns 200")
void initHourlyPattern_returns200() throws Exception {
    mockMvc.perform(post("/api/hourly-pattern/init"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));
    verify(trafficFlowService).initIfEmpty(anyString());
}
```

- [ ] `./gradlew test` 실행 후 전체 통과 확인

---

## Chunk 2: 프론트엔드

### Task 4: index.html — 페이지 로더 오버레이 추가

**Files:**
- Modify: `src/main/resources/templates/index.html`
- Modify: `src/main/resources/static/css/style.css`

- [ ] `<body>` 바로 아래 첫 번째 자식으로 오버레이 HTML 삽입

```html
<!-- ===== Page Loader Overlay ===== -->
<div id="pageLoader">
    <div class="page-loader-spinner"></div>
</div>
```

- [ ] `style.css`에 오버레이 + 스피너 CSS 추가

```css
/* ===== Page Loader Overlay ===== */
#pageLoader {
    position: fixed;
    inset: 0;
    background-color: #f5f6fa;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    transition: opacity 0.3s ease;
}

#pageLoader.fade-out {
    opacity: 0;
    pointer-events: none;
}

.page-loader-spinner {
    width: 48px;
    height: 48px;
    border: 4px solid #dee2e6;
    border-top-color: #0d6efd;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}
```

---

### Task 5: app.js — Promise.allSettled로 묶고 완료 시 오버레이 제거

**Files:**
- Modify: `src/main/resources/static/js/app.js`

- [ ] `loadHourlyPattern`을 `Promise`를 반환하도록 `traffic.js`에서 수정 (Task 6과 함께)
- [ ] `app.js`에서 3개 fetch를 `Promise.allSettled`로 묶고 오버레이 제거

```js
document.addEventListener('DOMContentLoaded', () => {
    initPageMeta();
    startCountdown();
    initSearchFilter();

    Promise.allSettled([
        loadSummary(),
        loadRanking(),
        loadHourlyPattern(),
    ]).then(() => {
        const loader = document.getElementById('pageLoader');
        if (loader) {
            loader.classList.add('fade-out');
            loader.addEventListener('transitionend', () => loader.remove(), { once: true });
        }
    });
});
```

---

### Task 6: traffic.js — loadHourlyPattern에 init+retry 추가

**Files:**
- Modify: `src/main/resources/static/js/traffic.js`

- [ ] `loadHourlyPattern()` 내 빈 응답 처리 시 init 호출 후 retry 로직 추가

```js
// 빈 데이터 → init 호출 후 1회 재시도
if (!items || items.length === 0) {
    try {
        await fetch('/api/hourly-pattern/init', { method: 'POST' });
        const retryRes = await fetch('/api/hourly-pattern');
        if (retryRes.ok) {
            const retryBody = await retryRes.json();
            const retryItems = retryBody.data;
            if (retryItems && retryItems.length > 0) {
                renderHourlyPattern(retryItems, currentHour);
                hideEl('hourlyLoading');
                showEl('hourlyTableWrap');
                return;
            }
        }
    } catch { /* ignore */ }
    hideEl('hourlyLoading');
    showEl('hourlyError');
    return;
}
```

- [ ] 기존 렌더링 로직을 `renderHourlyPattern(items, currentHour)` 함수로 추출

---

## 커밋 계획

| # | 커밋 메시지 | 파일 |
|---|------------|------|
| 1 | `fix(service): throw parse exceptions; fix(scheduler): guard refresh errors` | `TrafficFlowService.java`, `TrafficFlowScheduler.java` |
| 2 | `refactor(service): remove @PostConstruct, add initIfEmpty and null guard` | `TrafficFlowService.java`, `TrafficFlowServiceTest.java` |
| 3 | `feat(api): add POST /api/hourly-pattern/init endpoint` | `TrafficApiController.java`, `TrafficApiControllerTest.java` |
| 4 | `feat(config): add Feign global connect/read timeout` | `application.properties` |
| 5 | `feat(frontend): add global page loader overlay and hourly-pattern init-on-empty` | `index.html`, `style.css`, `app.js`, `traffic.js` |
