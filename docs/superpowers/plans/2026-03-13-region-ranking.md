# Region Ranking Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 권역별 출구 교통량 순위를 집계해 `/api/region-ranking` API와 대시보드 섹션으로 제공한다.

**Architecture:** `trafficRegion` API 응답을 `regionCode`로 groupBy 후 `trafficAmount`를 합산해 순위를 매긴다. 기존 `TrafficService`에 메서드를 추가하고, `TrafficApiController`에 엔드포인트 하나를 추가한다. 프론트엔드는 `traffic.js`에 fetch 함수를 추가하고 `index.html`에 카드 섹션을 추가한다.

**Tech Stack:** Spring Boot 3.5.0, Spring Cloud OpenFeign, Caffeine Cache, Lombok, JUnit 5 + Mockito, Bootstrap 5, Vanilla JS (ES module)

---

## 파일 맵

| 구분 | 파일 | 변경 내용 |
|------|------|-----------|
| 수정 | `client/response/TrafficRegionResponse.java` | `@JsonProperty("list")` → `@JsonProperty("trafficRegion")` 버그 수정 |
| 수정 | `client/response/TrafficRegionItem.java` | `sumDate` 필드 추가 |
| 생성 | `dto/RegionTrafficDto.java` | 권역 순위 DTO |
| 수정 | `service/TrafficService.java` | `getRegionRanking()` 메서드 추가 |
| 수정 | `config/CacheConfig.java` | `regionRanking` 캐시 5분 TTL 추가 |
| 수정 | `controller/TrafficApiController.java` | `GET /api/region-ranking` 엔드포인트 추가 |
| 수정 | `static/js/traffic.js` | `loadRegionRanking()` 함수 추가 |
| 수정 | `templates/index.html` | 권역 순위 카드 섹션 추가 |
| 수정 | `static/js/app.js` | `loadRegionRanking` import 및 호출 추가 |
| 수정 | `test/.../TrafficServiceTest.java` | `getRegionRanking` 테스트 추가 |
| 수정 | `test/.../TrafficApiControllerTest.java` | `/api/region-ranking` 테스트 추가 |

---

## Task 1: `TrafficRegionResponse` `@JsonProperty` 버그 수정 + `sumDate` 필드 추가

실제 API 응답의 최상위 키는 `"trafficRegion"`이지만 현재 `@JsonProperty("list")`로 잘못 매핑되어 있다. 또한 `sumDate` 필드가 응답에는 있으나 `TrafficRegionItem`에 없다.

**Files:**
- Modify: `src/main/java/com/vroomtracker/client/response/TrafficRegionResponse.java`
- Modify: `src/main/java/com/vroomtracker/client/response/TrafficRegionItem.java`

- [ ] **Step 1: 버그를 증명하는 실패 테스트 작성**

`src/test/java/com/vroomtracker/client/response/TrafficRegionResponseDeserializeTest.java`를 새로 만든다:

```java
package com.vroomtracker.client.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficRegionResponseDeserializeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialize_trafficRegionField_mapsToList() throws Exception {
        String json = """
                {
                  "code": "SUCCESS",
                  "message": "인증키가 유효합니다.",
                  "count": "1",
                  "trafficRegion": [
                    {
                      "regionCode": "927",
                      "regionName": "전북본부",
                      "trafficAmout": "100",
                      "tcsType": "1",
                      "carType": "1",
                      "openClType": "0",
                      "exDivCode": "00",
                      "inoutType": "1",
                      "tmType": "2",
                      "sumTm": "0900",
                      "sumDate": "20260313"
                    }
                  ]
                }
                """;

        TrafficRegionResponse response = mapper.readValue(json, TrafficRegionResponse.class);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getList()).isNotNull().hasSize(1);
        assertThat(response.getList().get(0).getRegionCode()).isEqualTo("927");
        assertThat(response.getList().get(0).getSumDate()).isEqualTo("20260313");
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
cd /Users/jangjisu/vroom-tracker
./gradlew test --tests "com.vroomtracker.client.response.TrafficRegionResponseDeserializeTest" -i
```

Expected: FAIL — `getList()` returns null (잘못된 `@JsonProperty` 때문)

- [ ] **Step 3: `TrafficRegionResponse` 버그 수정**

```java
// @JsonProperty("list") → 변경
@JsonProperty("trafficRegion")
private List<TrafficRegionItem> list;
```

- [ ] **Step 4: `TrafficRegionItem`에 `sumDate` 필드 추가**

```java
/** 집계일자 (yyyyMMdd) */
private String sumDate;
```

- [ ] **Step 5: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.vroomtracker.client.response.TrafficRegionResponseDeserializeTest" -i
```

Expected: PASS

- [ ] **Step 6: 전체 테스트 통과 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/vroomtracker/client/response/TrafficRegionResponse.java \
        src/main/java/com/vroomtracker/client/response/TrafficRegionItem.java \
        src/test/java/com/vroomtracker/client/response/TrafficRegionResponseDeserializeTest.java
git commit -m "fix: correct @JsonProperty for trafficRegion response field and add sumDate"
```

---

## Task 2: `RegionTrafficDto` 생성

**Files:**
- Create: `src/main/java/com/vroomtracker/dto/RegionTrafficDto.java`

- [ ] **Step 1: DTO 클래스 작성**

```java
package com.vroomtracker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionTrafficDto {

    /** 순위 */
    private final int rank;

    /** 권역코드 */
    private final String regionCode;

    /** 권역명 */
    private final String regionName;

    /** 총 교통량 (대) */
    private final long totalVolume;

    /** 총 교통량 표시용 문자열 */
    private final String formattedVolume;

    /** 막대그래프 너비 0~100 (최대값 기준 비율) */
    private final int barWidth;

    /** 집계시간 */
    private final String sumTm;

    /**
     * 서비스에서 컬렉션 수준 계산(rank, totalVolume, maxVol, sumTm 포맷) 완료 후 호출.
     */
    public static RegionTrafficDto of(int rank, String regionCode, String regionName,
                                      long totalVolume, long maxVolume, String formattedSumTm) {
        return RegionTrafficDto.builder()
                .rank(rank)
                .regionCode(regionCode)
                .regionName(regionName)
                .totalVolume(totalVolume)
                .formattedVolume(String.format("%,d 대", totalVolume))
                .barWidth(maxVolume > 0 ? (int) (totalVolume * 100 / maxVolume) : 0)
                .sumTm(formattedSumTm)
                .build();
    }
}
```

- [ ] **Step 2: 전체 테스트 통과 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/vroomtracker/dto/RegionTrafficDto.java
git commit -m "feat: add RegionTrafficDto for region ranking"
```

---

## Task 3: `CacheConfig`에 `regionRanking` 캐시 추가

**Files:**
- Modify: `src/main/java/com/vroomtracker/config/CacheConfig.java`

- [ ] **Step 1: `regionRanking` 캐시 빈 추가**

`cacheManager()` 메서드에서 `dashboard` 캐시 아래에 추가하고 `List.of()`에 포함한다:

```java
var regionRanking = new CaffeineCache("regionRanking",
        Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build());

// manager.setCaches(List.of(dashboard)); 를 아래로 교체
manager.setCaches(List.of(dashboard, regionRanking));
```

주석도 업데이트:
```java
/**
 * 캐시 전략:
 * - dashboard: 5분 TTL — getDashboardData() (trafficIc API 기반)
 * - regionRanking: 5분 TTL — getRegionRanking() (trafficRegion API 기반)
 *
 * trafficFlowByTime 데이터는 DB에서 읽으므로 별도 캐시 불필요.
 */
```

- [ ] **Step 2: 전체 테스트 통과 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/vroomtracker/config/CacheConfig.java
git commit -m "feat: add regionRanking cache (5min TTL)"
```

---

## Task 4: `TrafficService.getRegionRanking()` 구현

`sumTm`은 `"0900"` (HHMM, 4자리), `sumDate`는 `"20260313"` (8자리)로 분리 제공된다. 두 값을 합치면 `"202603130900"` (12자리)가 되어 기존 `formatSumTm()`의 12자리 분기(`yyyyMMddHHmm`)와 일치한다.

**Files:**
- Modify: `src/main/java/com/vroomtracker/service/TrafficService.java`
- Modify: `src/test/java/com/vroomtracker/service/TrafficServiceTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`TrafficServiceTest`에 `RegionRanking` 중첩 클래스를 추가한다:

```java
// import 추가
import com.vroomtracker.client.response.TrafficRegionItem;
import com.vroomtracker.client.response.TrafficRegionResponse;
import com.vroomtracker.dto.RegionTrafficDto;

// 클래스 하단 헬퍼 영역에 추가
private TrafficRegionItem regionItem(String regionCode, String regionName, String amount,
                                     String sumTm, String sumDate) {
    TrafficRegionItem item = new TrafficRegionItem();
    item.setRegionCode(regionCode);
    item.setRegionName(regionName);
    item.setTrafficAmount(amount);
    item.setSumTm(sumTm);
    item.setSumDate(sumDate);
    return item;
}

private void stubRegionApi(List<TrafficRegionItem> items) {
    TrafficRegionResponse response = new TrafficRegionResponse();
    response.setCode("SUCCESS");
    response.setList(items);
    when(exApiClient.getTrafficRegion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(response);
}

// 중첩 클래스 추가
@Nested
@DisplayName("getRegionRanking")
class RegionRankingTest {

    @Test
    @DisplayName("regionRanking_aggregatesByRegionCode")
    void regionRanking_aggregatesByRegionCode() {
        stubRegionApi(List.of(
                regionItem("927", "전북본부", "100", "0900", "20260313"),
                regionItem("927", "전북본부", "200", "0900", "20260313"),
                regionItem("905", "대구경북본부", "500", "0900", "20260313")
        ));

        List<RegionTrafficDto> result = trafficService.getRegionRanking();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRegionCode()).isEqualTo("905");
        assertThat(result.get(0).getTotalVolume()).isEqualTo(500L);
        assertThat(result.get(1).getTotalVolume()).isEqualTo(300L);
    }

    @Test
    @DisplayName("regionRanking_assignsRankStartingFromOne")
    void regionRanking_assignsRankStartingFromOne() {
        stubRegionApi(List.of(
                regionItem("905", "대구경북본부", "500", "0900", "20260313"),
                regionItem("927", "전북본부", "300", "0900", "20260313")
        ));

        List<RegionTrafficDto> result = trafficService.getRegionRanking();

        assertThat(result).extracting(RegionTrafficDto::getRank).containsExactly(1, 2);
    }

    @Test
    @DisplayName("regionRanking_topRankHasBarWidth100")
    void regionRanking_topRankHasBarWidth100() {
        stubRegionApi(List.of(
                regionItem("905", "대구경북본부", "500", "0900", "20260313"),
                regionItem("927", "전북본부", "250", "0900", "20260313")
        ));

        List<RegionTrafficDto> result = trafficService.getRegionRanking();

        assertThat(result.get(0).getBarWidth()).isEqualTo(100);
        assertThat(result.get(1).getBarWidth()).isEqualTo(50);
    }

    @Test
    @DisplayName("regionRanking_whenApiFailureCode_returnsEmptyList")
    void regionRanking_whenApiFailureCode_returnsEmptyList() {
        TrafficRegionResponse response = new TrafficRegionResponse();
        response.setCode("99");
        when(exApiClient.getTrafficRegion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(response);

        assertThat(trafficService.getRegionRanking()).isEmpty();
    }

    @Test
    @DisplayName("regionRanking_whenFeignThrows_returnsEmptyList")
    void regionRanking_whenFeignThrows_returnsEmptyList() {
        when(exApiClient.getTrafficRegion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("timeout"));

        assertThat(trafficService.getRegionRanking()).isEmpty();
    }

    @Test
    @DisplayName("regionRanking_formatsVolume")
    void regionRanking_formatsVolume() {
        stubRegionApi(List.of(
                regionItem("905", "대구경북본부", "1234", "0900", "20260313")
        ));

        RegionTrafficDto dto = trafficService.getRegionRanking().get(0);

        assertThat(dto.getFormattedVolume()).isEqualTo("1,234 대");
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.vroomtracker.service.TrafficServiceTest" -i
```

Expected: FAIL — `getRegionRanking()` 메서드 없음

- [ ] **Step 3: `TrafficService`에 `getRegionRanking()` 구현**

`TrafficService.java`에 추가 (import, 필드, 메서드):

```java
// import 추가
import com.vroomtracker.client.response.TrafficRegionItem;
import com.vroomtracker.client.response.TrafficRegionResponse;
import com.vroomtracker.dto.RegionTrafficDto;
import java.util.Map;
import java.util.Objects;

// @Cacheable 메서드 추가 (getDashboardData 아래)
@Cacheable(value = "regionRanking")
public List<RegionTrafficDto> getRegionRanking() {
    List<TrafficRegionItem> items = fetchRegionTraffic();
    return buildRegionRanking(items);
}

// private fetch 메서드 추가
private List<TrafficRegionItem> fetchRegionTraffic() {
    try {
        TrafficRegionResponse response =
                exApiClient.getTrafficRegion(apiKey, JSON,
                        null, null, InoutType.EXIT.value(),
                        null, null, null, TmType.FIFTEEN_MIN.value());

        if (!response.isSuccess()) {
            log.warn("trafficRegion API 실패: code={}, message={}", response.getCode(), response.getMessage());
            return Collections.emptyList();
        }

        List<TrafficRegionItem> list = response.getList();
        return list != null ? list : Collections.emptyList();

    } catch (Exception e) {
        log.error("trafficRegion API 호출 실패", e);
        return Collections.emptyList();
    }
}

// private 집계 메서드 추가
private List<RegionTrafficDto> buildRegionRanking(List<TrafficRegionItem> items) {
    if (items.isEmpty()) return Collections.emptyList();

    record RegionSummary(String regionCode, String regionName, long totalVolume, String sumTm) {}

    List<RegionSummary> aggregated = items.stream()
            .filter(i -> i.getTrafficAmount() != null && !i.getTrafficAmount().isBlank())
            .collect(Collectors.groupingBy(TrafficRegionItem::getRegionCode))
            .entrySet().stream()
            .map(e -> {
                List<TrafficRegionItem> group = e.getValue();
                String regionName = group.get(0).getRegionName();
                long total = group.stream()
                        .mapToLong(i -> (long) parseAmount(i.getTrafficAmount()))
                        .sum();
                String rawSumTm = group.stream()
                        .map(i -> i.getSumDate() != null && i.getSumTm() != null
                                ? i.getSumDate() + i.getSumTm() : "")
                        .filter(s -> !s.isBlank())
                        .max(Comparator.naturalOrder())
                        .orElse("-");
                return new RegionSummary(e.getKey(), regionName, total, formatSumTm(rawSumTm));
            })
            .sorted(Comparator.comparingLong(RegionSummary::totalVolume).reversed())
            .toList();

    if (aggregated.isEmpty()) return Collections.emptyList();
    long maxVol = aggregated.get(0).totalVolume();

    return IntStream.range(0, aggregated.size())
            .mapToObj(i -> {
                RegionSummary s = aggregated.get(i);
                return RegionTrafficDto.of(i + 1, s.regionCode(), s.regionName(),
                        s.totalVolume(), maxVol, s.sumTm());
            })
            .toList();
}
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.vroomtracker.service.TrafficServiceTest" -i
```

Expected: PASS

- [ ] **Step 5: 전체 테스트 통과 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/vroomtracker/service/TrafficService.java \
        src/test/java/com/vroomtracker/service/TrafficServiceTest.java
git commit -m "feat: add getRegionRanking() to TrafficService"
```

---

## Task 5: `GET /api/region-ranking` 엔드포인트 추가

**Files:**
- Modify: `src/main/java/com/vroomtracker/controller/TrafficApiController.java`
- Modify: `src/test/java/com/vroomtracker/controller/TrafficApiControllerTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`TrafficApiControllerTest`에 추가:

```java
// import 추가
import com.vroomtracker.dto.RegionTrafficDto;

@Test
@DisplayName("GET /api/region-ranking returns 200 with ApiResponse wrapper")
void getRegionRanking_returns200WithApiResponseWrapper() throws Exception {
    RegionTrafficDto dto = RegionTrafficDto.of(1, "905", "대구경북본부", 12450L, 12450L, "2026-03-13 09:00");
    when(trafficService.getRegionRanking()).thenReturn(List.of(dto));

    mockMvc.perform(get("/api/region-ranking"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].regionName").value("대구경북본부"))
            .andExpect(jsonPath("$.data[0].rank").value(1));
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.vroomtracker.controller.TrafficApiControllerTest" -i
```

Expected: FAIL — 404 (엔드포인트 없음)

- [ ] **Step 3: 엔드포인트 추가**

`TrafficApiController`에 추가:

```java
// import 추가
import com.vroomtracker.dto.RegionTrafficDto;

@GetMapping("/region-ranking")
public ResponseEntity<ApiResponse<List<RegionTrafficDto>>> getRegionRanking() {
    return ResponseEntity.ok(ApiResponse.success(trafficService.getRegionRanking()));
}
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.vroomtracker.controller.TrafficApiControllerTest" -i
```

Expected: PASS

- [ ] **Step 5: 전체 테스트 통과 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/vroomtracker/controller/TrafficApiController.java \
        src/test/java/com/vroomtracker/controller/TrafficApiControllerTest.java
git commit -m "feat: add GET /api/region-ranking endpoint"
```

---

## Task 6: 프론트엔드 — 권역 순위 섹션 추가

**Files:**
- Modify: `src/main/resources/static/js/traffic.js`
- Modify: `src/main/resources/templates/index.html`
- Modify: `src/main/resources/static/js/app.js`

- [ ] **Step 1: `traffic.js`에 `loadRegionRanking()` 추가**

파일 하단에 추가:

```js
// ===== 권역별 교통량 순위 =====

export async function loadRegionRanking() {
    try {
        const res = await fetch('/api/region-ranking');
        if (!res.ok) throw new Error(res.status);
        const body = await res.json();
        if (body.code === 'EXTERNAL_API_UNAVAILABLE') {
            showApiUnavailableAlert();
            hideEl('regionLoading');
            showEl('regionError');
            return;
        }
        const items = body.data;

        if (!items || items.length === 0) {
            hideEl('regionLoading');
            showEl('regionError');
            return;
        }

        document.getElementById('regionBody').innerHTML =
            items.map(item => buildRegionRow(item)).join('');

        hideEl('regionLoading');
        showEl('regionTableWrap');
    } catch {
        hideEl('regionLoading');
        showEl('regionError');
    }
}

function buildRegionRow(item) {
    const rankBadge = item.rank <= 3
        ? `<span class="rank-badge ${rankClass(item.rank)}">${item.rank}</span>`
        : `<span class="text-muted fw-bold">${item.rank}</span>`;

    return `<tr>
        <td class="text-center align-middle">${rankBadge}</td>
        <td class="align-middle fw-semibold">${item.regionName ?? '-'}</td>
        <td class="align-middle text-end fw-bold">${item.formattedVolume ?? '-'}</td>
        <td class="align-middle">
            <div class="traffic-bar-bg">
                <div class="traffic-bar bar-medium" style="width:${item.barWidth ?? 0}%"></div>
            </div>
        </td>
        <td class="align-middle text-muted small">${item.sumTm ?? '-'}</td>
    </tr>`;
}
```

- [ ] **Step 2: `index.html`에 권역 순위 카드 추가**

기존 랭킹 테이블 카드(`<!-- ===== 메인 랭킹 테이블 ===== -->`)와 시간대별 패턴 카드 사이에 삽입:

```html
<!-- ===== 권역별 교통량 순위 ===== -->
<div class="card mb-4">
    <div class="card-header">
        <h6 class="mb-0 fw-bold">
            <i class="bi bi-map me-2"></i>권역별 출구 교통량 순위
            <span class="badge bg-secondary ms-1 fw-normal small">15분 집계 기준</span>
        </h6>
    </div>

    <div class="card-body p-0">
        <!-- 로딩 상태 -->
        <div id="regionLoading" class="text-center py-5 text-muted">
            <div class="spinner-border spinner-border-sm me-2" role="status"></div>
            권역 데이터 불러오는 중...
        </div>

        <!-- 오류 상태 -->
        <div id="regionError" class="text-center py-5 text-muted" style="display:none;">
            <i class="bi bi-wifi-off fs-2 d-block mb-2"></i>
            데이터를 불러오지 못했습니다.
        </div>

        <!-- 권역 순위 테이블 -->
        <div id="regionTableWrap" style="display:none;">
            <table class="table table-hover mb-0">
                <thead class="table-dark">
                <tr>
                    <th class="text-center" style="width:60px">순위</th>
                    <th>권역</th>
                    <th class="text-end">출구 교통량</th>
                    <th style="min-width:120px">현황</th>
                    <th class="text-muted small">집계시간</th>
                </tr>
                </thead>
                <tbody id="regionBody"></tbody>
            </table>
        </div>
    </div>
</div>
```

- [ ] **Step 3: `app.js`에 import 및 호출 추가**

```js
// 기존 import 라인 수정
import { loadSummary, loadRanking, loadHourlyPattern, loadRegionRanking } from './traffic.js';

// Promise.allSettled 배열에 추가
Promise.allSettled([
    loadSummary(),
    loadRanking(),
    loadRegionRanking(),
    loadHourlyPattern(),
]).then( ... );
```

- [ ] **Step 4: 전체 테스트 통과 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 커밋**

```bash
git add src/main/resources/static/js/traffic.js \
        src/main/resources/templates/index.html \
        src/main/resources/static/js/app.js
git commit -m "feat: add region ranking section to dashboard"
```

---

## 완료 체크

```bash
./gradlew test
```

모든 테스트 PASS 확인 후 구현 완료.
