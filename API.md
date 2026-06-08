# API.md — 한국도로공사 OpenAPI 연동 노트

> 베이스 URL: `https://data.ex.co.kr`
> 현재 코드에 실제로 연결된 API만 기록한다.

---

## 현재 연동 API

### locationinfoRest — 휴게소 위치 정보

전국 고속도로 휴게소의 위치 좌표와 기본 정보를 반환한다.

현재 코드에서는 다음 파일이 이 API를 담당한다.

- `src/main/java/com/vroomtracker/client/ExApiClient.java`
- `src/main/java/com/vroomtracker/client/ExApiFeignClient.java`
- `src/main/java/com/vroomtracker/client/response/RestStopResponse.java`
- `src/main/java/com/vroomtracker/client/response/RestStopItem.java`

### 엔드포인트

```text
GET https://data.ex.co.kr/openapi/locationinfo/locationinfoRest
```

### 요청 파라미터

현재 `ExApiClient`에 정의된 파라미터는 다음과 같다.

| 파라미터 | 조건 | 설명 |
|---|---|---|
| `key` | 필수 | 인증키 |
| `type` | 필수 | 현재 앱에서는 `json`만 사용 |
| `numOfRows` | 선택 | 페이지당 결과 수 |
| `pageNo` | 선택 | 페이지 번호 |

한국도로공사 문서상 `unitCode`, `routeNo`, `stdRestCd`, `serviceAreaCode` 등 추가 선택 파라미터가 있을 수 있으나, 현재 코드에는 아직 연결하지 않았다.

### 응답 구조

현재 응답 VO는 다음 필드를 가진다.

```json
{
  "code": "SUCCESS",
  "message": "인증키가 유효합니다.",
  "count": "203",
  "pageSize": "3",
  "list": [
    {
      "unitCode": "001",
      "unitName": "서울만남(부산)휴게소",
      "routeNo": "0010",
      "routeName": "경부선",
      "xValue": "127.042514",
      "yValue": "37.459939",
      "stdRestCd": "000001",
      "serviceAreaCode": "A00001"
    }
  ]
}
```

### 코드 기준 처리

- 성공 여부는 `RestStopResponse.isSuccess()`에서 판단한다.
- 현재 성공 코드는 `"SUCCESS"`로 판단한다.
- `ExApiClient` wrapper가 인증키, `json` 포맷, `numOfRows=99` 기본값을 적용한다.
- `pageSize`는 실제 응답 기준 총 페이지 수로 보고 `getTotalPageCount()`에서 정수 변환한다.
- `pageSize`가 숫자가 아니면 기본값 `1`을 반환한다.

---

## 다음 API를 추가할 때 기록할 것

새 공공 API를 연결할 때는 추정값이 아니라 실제 호출 결과 기준으로 아래 항목을 남긴다.

- 엔드포인트
- 요청 파라미터
- 응답 최상위 필드
- list 필드명
- 성공 코드
- 페이지네이션 방식
- 실제 샘플 응답

성공 코드와 응답 필드명은 API마다 다를 수 있으므로 기존 API 값을 그대로 재사용하지 않는다.
