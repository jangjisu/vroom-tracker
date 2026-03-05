# vroom-tracker

고속도로 톨게이트별 실시간 출구 교통량 대시보드.
"지금 사람들이 어디로 많이 가는지" 랭킹으로 보여주어 여행지 탐색에 활용합니다.

## 기술 스택

| 항목 | 기술 |
|---|---|
| Backend | Spring Boot 3.5.0, Java 17 |
| Template | Thymeleaf |
| HTTP Client | Spring Cloud OpenFeign |
| Cache | Caffeine (in-memory) |
| 기타 | Lombok, Bootstrap 5 |

## 사용 API (data.ex.co.kr)

| API | 엔드포인트 | 용도 | 갱신 주기 |
|---|---|---|---|
| 톨게이트 입/출구 교통량 | `/openapi/trafficapi/trafficIc` | 메인 랭킹 테이블 | 15분 집계 |
| 실시간 전국 교통량 | `/openapi/trafficapi/trafficAll` | 상단 요약 카드 | 5분 집계 |
| 시간대별 교통량 현황 | `/openapi/specialAnal/trafficFlowByTime` | 시간대 패턴 테이블 | 연간 통계 |

> API 키는 [data.ex.co.kr](https://data.ex.co.kr) 에서 발급받아야 합니다.

### 주요 파라미터

**trafficIc**
- `inoutType=1` (출구), `tmType=2` (15분 집계), `numOfRows=500`
- 응답 필드: `unitCode`, `unitName`, `trafficAmount`(만대), `sumTm`, `exDivName`

**trafficAll**
- `tmType=3` (5분 집계)
- 응답 필드: `trafficAmout` (**오타 주의**, API 문서 그대로), `sumTm`

**trafficFlowByTime**
- `iStdYear` (기준년)
- 응답 필드: `stdHour`, `trfl`(교통량), `sphlDfttNm`(특수일구분명)

## 캐싱 전략 (DB 없음)

실시간 현황 대시보드는 과거 데이터 저장이 불필요하므로 Caffeine 인메모리 캐시 사용.

| 캐시명 | TTL | 이유 |
|---|---|---|
| `trafficIc` | 5분 | API 15분 집계, 5분 내 재호출 방지 |
| `trafficAll` | 5분 | API 5분 집계 |
| `trafficFlow` | 1일 | 연간 통계 데이터, 변경 거의 없음 |

화면 자동 갱신: **5분** (캐시 TTL 기준)

## 화면 구성

```
[ 상단 요약 카드 3개 ]
  전국 출구 교통량 합계 | 혼잡 영업소 수 | 가장 붐비는 영업소

[ 메인 랭킹 테이블 ]
  순위 | 영업소 | 구분(도공/민자) | 출구 교통량 | 혼잡도 | 막대그래프 | 집계시간

[ 영업소명 검색 필터 ]
  텍스트 입력으로 영업소명 필터링

[ 시간대별 교통량 패턴 (연간 통계) ]
  현재 시간대 강조 표시

[ 자동 갱신 카운트다운 (5분) ]
```

## 로컬 실행 방법

### 1. API 키 설정

`src/main/resources/application-local.properties` 파일:
```properties
ex.api.key=YOUR_API_KEY_HERE
```

### 2. 실행

```bash
./gradlew bootRun
```

`http://localhost:8080` 접속

## 혼잡도 기준 (조정 필요)

| 수준 | 교통량 (만대) | 표시 |
|---|---|---|
| 많음 | 5.0 이상 | 빨간색 |
| 보통 | 2.0 ~ 4.9 | 노란색 |
| 적음 | 2.0 미만 | 초록색 |

> 실제 데이터를 확인한 후 `TrafficService.java`의 `HIGH_THRESHOLD`, `MEDIUM_THRESHOLD` 상수를 조정하세요.

## 주의사항

- `trafficAll` 응답의 `trafficAmout` 필드는 API 문서의 오타입니다. 실제 응답 키가 다를 경우 `ExApiClient.java`의 `@JsonProperty("trafficAmout")`를 수정하세요.
- `trafficIc` 응답의 응답 배열 필드명이 `list`가 아닐 수 있습니다. 실제 응답을 확인 후 `ExApiClient.TrafficIcResponse`의 `@JsonProperty`를 수정하세요.
- 노선명은 API에서 제공되지 않습니다. 영업소명 텍스트 검색으로 대체합니다.

## 프로젝트 구조

```
src/main/java/com/vroomtracker/
├── config/
│   └── CacheConfig.java             # Caffeine 캐시 TTL 설정
├── controller/
│   └── TrafficController.java
├── service/
│   └── TrafficService.java          # 데이터 가공, @Cacheable
├── client/
│   └── ExApiClient.java             # Feign Client + 응답 타입
├── dto/
│   ├── TollGateTrafficDto.java
│   └── NationwideTrafficDto.java
└── VroomTrackerApplication.java
```
