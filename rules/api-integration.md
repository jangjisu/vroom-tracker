# 새 API 연동 시 확인 규칙

## 순서

1. `API.md` 를 먼저 읽는다 — 연동 전 기존 명세·실측값 파악
2. `ExApiClient` 메서드 추가
3. 응답 VO 작성
4. 실제 호출 후 `API.md` 업데이트

## API.md 업데이트 규칙

- 실측으로 확인한 사실은 즉시 반영한다 (성공 코드, 응답 list 필드명, count 위치 등)
- `⚠️ 미실측` 표시 항목은 실제 호출 전까지 추정값으로 취급한다
- 응답 샘플은 실제 API 응답을 그대로 사용한다 (가공하지 않음)

## 응답 VO 작성 규칙

### isSuccess() 위치

성공 여부 판단은 응답 VO 안에 `isSuccess()` 로 정의한다.
서비스에서 `getCode()` 를 꺼내 인라인 비교하지 않는다.

```java
// ❌
if (!"00".equals(response.getCode())) { ... }

// ✅
public boolean isSuccess() {
    return "SUCCESS".equals(code);
}
```

### 성공 코드 실측 원칙

성공 코드를 가정하지 않는다. 반드시 실제 호출 후 확인하고 `API.md` 에 기록한다.

## 파라미터 매직 넘버 관리

API 호출 시 코드값·설정값을 서비스 코드에 문자열 리터럴로 직접 넣지 않는다.

| 유형 | 관리 방식 |
|------|----------|
| 의미 있는 코드값 (API 스펙 정의) | Enum |
| 환경·운영에 따라 조정 가능한 값 | application.properties |
| 포맷 고정값 (변경 가능성 없음) | `private static final` 상수 |

```java
// ❌
exApiClient.getLocationInfoRest(apiKey, "json", "99", "1");

// ✅
exApiClient.getLocationInfoRest(apiKey, FORMAT_JSON, NUM_OF_ROWS, pageNo);
```
