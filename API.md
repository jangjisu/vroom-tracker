# API.md — 한국도로공사 OpenAPI 연동 노트

> 베이스 URL: `https://data.ex.co.kr`
> 현재 코드에 실제로 연결된 API만 기록한다.

---

## 공통 요청 헤더

한국도로공사 OpenAPI를 호출하는 `ex-api` Feign Client는 모든 요청에 다음 헤더를 보낸다.

```text
User-Agent: vroom-tracker
Accept: application/json
```

동일 endpoint를 호출해 다음 결과를 실측했다.

| 요청 조건 | 결과 |
|---|---|
| 기본 `curl/8.7.1` User-Agent | `400 Request Blocked` |
| User-Agent 없음 | `400 Request Blocked` |
| `User-Agent: vroom-tracker` | `200 OK` |

HTTP/1.1 강제 여부는 결과에 영향을 주지 않았다. endpoint, query parameter와 response 구조는 변경되지 않는다.

### 공통 실패 exception

`ExApiClient`는 빈 응답, API 실패 응답, Feign·네트워크·디코딩 예외를 다음 형식의 `ExApiException`으로 변환한다.

```text
Failed to fetch API. requestUrl=<실제 요청 URL>, message=<실패 원인>
```

`requestUrl`에는 endpoint와 실제 호출한 모든 query parameter가 포함된다. 장애 로그에서 URL을 바로 실행해 재현할 수 있도록 `key`도 마스킹하지 않고 runtime 값을 그대로 포함한다.

```text
Failed to fetch API. requestUrl=https://data.ex.co.kr/openapi/business/conveniServiceArea?key=4041555581&type=json&numOfRows=99&pageNo=2, message=For input string: ""
```

API 키 평문 포함은 로그 접근자와 외부 로그 저장소에 키가 노출될 수 있는 운영상 위험이 있다. 이 프로젝트에서는 장애 재현 편의성을 우선한 명시적 결정으로 적용한다.

---

## 현재 연동 API

### conveniServiceArea — 휴게소 편의시설 정보

#### 서버 오류 응답

동일 요청에서 `Accept` 헤더에 따라 같은 upstream 서버 오류가 다른 형식으로 반환되는 것을 확인했다.

| 요청 조건 | HTTP 상태 | Content-Type | 본문 |
|---|---|---|---|
| `Accept` 없음 또는 HTML 선호 | `200 OK` | `text/html` | 시스템 장애 HTML |
| `Accept: application/json` | `200 OK` | `application/json` | `exception.message`를 포함한 JSON |

실측한 upstream 오류 메시지는 다음과 같다.

```text
For input string: ""
```

stack trace상 한국도로공사 서버의 `ConveniServiceAreaListVO`가 빈 문자열을 숫자로 변환하다 실패한 오류다. 오류인데도 HTTP 상태가 `200 OK`이므로 상태 코드만으로 성공을 판단하지 않고 `code`와 `exception.message`를 확인해야 한다.

현재 `RestStopDetailResponse.getErrorMessage()`는 다음 우선순위로 오류 메시지를 반환한다.

1. upstream 서버 오류의 `exception.message`
2. 인증키 오류 등 일반 API 실패 응답의 최상위 `message`

---

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

### restOilList — 휴게소 편의시설 현황(주유소)

전국 고속도로 주유소의 편의시설과 운영시간을 반환한다.

#### 엔드포인트

```text
GET https://data.ex.co.kr/openapi/restinfo/restOilList
```

#### 요청 파라미터

| 파라미터 | 조건 | 설명 |
|---|---|---|
| `key` | 필수 | 인증키 |
| `type` | 필수 | 현재 앱에서는 `json`만 사용 |
| `routeCd` | 선택 | 노선코드 |
| `routeNm` | 선택 | 노선명 |
| `stdRestCd` | 선택 | 휴게소/주유소코드 |
| `stdRestNm` | 선택 | 휴게소/주유소명 |

현재 `ExApiClient`는 전체 목록 조회를 위해 `key`, `type`만 전달한다.
페이지네이션 파라미터는 없으며 전체 목록이 한 응답으로 반환된다.

#### 2026-06-15 실측 결과

- HTTP 상태: `200 OK`
- Content-Type: `application/json; charset=utf-8`
- 성공 코드: `"SUCCESS"`
- `count`: 숫자 `429`
- 동일한 `stdRestCd`에 서로 다른 `psCode` 행이 여러 개 존재한다.
- `routeNm`과 `psDesc`는 `null`일 수 있다.
- 제공 문서에는 `count`가 string으로 표기되어 있지만 실제 JSON은 숫자다.

```json
{
  "list": [
    {
      "stdRestCd": "000002",
      "stdRestNm": "서울만남(부산)주유소",
      "stime": "00:00",
      "etime": "24:00",
      "redId": "MANJ03",
      "redDtime": "16/03/10",
      "lsttmAltrUser": "SYSTEM",
      "lsttmAltrDttm": "2026-06-15",
      "svarAddr": "서울시 서초구 원지동10-16",
      "routeCd": "0010",
      "routeNm": "경부선",
      "psCode": "07",
      "psName": "쉼터",
      "psDesc": "고객쉼터-안마기,컴퓨터,팩스,혈압계,신장및체중계,핸드폰충전기,핸드폰소독기"
    }
  ],
  "count": 429,
  "message": "인증키가 유효합니다.",
  "code": "SUCCESS"
}
```

#### 코드 기준 처리

- 성공 여부는 `RestOilResponse.isSuccess()`에서 판단한다.
- `count`는 실측 JSON 타입에 맞춰 `int`로 역직렬화한다.
- 원본 행은 `RestOilItem`에 그대로 보존하며 중복처럼 보이는 휴게소 코드를 합치지 않는다.
- 빈 응답, 실패 코드와 Feign 예외는 `ExApiClient` 공통 `fetch()`에서 처리한다.

---

### curStateStation — 주유소 가격 현황

전국 고속도로 주유소의 현재 가격과 정유사, LPG 여부, 전화번호, 주소를 반환한다.

#### 엔드포인트

```text
GET https://data.ex.co.kr/openapi/business/curStateStation
```

#### 요청 파라미터

| 파라미터 | 조건 | 설명 |
|---|---|---|
| `key` | 필수 | 인증키 |
| `type` | 필수 | 현재 앱에서는 `json`만 사용 |
| `numOfRows` | 선택 | 페이지당 결과 수 |
| `pageNo` | 선택 | 페이지 번호 |
| `oilCompany` | 선택 | 정유사 |
| `routeCode` | 선택 | 노선코드 |
| `serviceAreaCode` | 선택 | 영업부대시설코드 |
| `routeName` | 선택 | 노선명 |
| `serviceAreaCode2` | 선택 | 휴게소/주유소코드 |
| `serviceAreaName` | 선택 | 휴게소/주유소명 |
| `direction` | 선택 | 방향 |

현재 `ExApiClient`는 전체 목록 조회를 위해 `key`, `type`, `numOfRows=99`, `pageNo=1..3`만 전달한다.

#### 2026-06-16 실측 결과

- HTTP 상태: `200 OK`
- Content-Type: `application/json; charset=utf-8`
- 성공 코드: `"SUCCESS"`
- `count`: 숫자 `226`
- `pageSize`: 숫자 `3`
- `pageNo`, `numOfRows`: 최상위는 숫자, list 내부의 동일 필드는 `null`
- `diselPrice`는 API 원문 오탈자 그대로 응답되며 코드에서는 `dieselPrice`로 매핑한다.
- 가격은 `"1,999원"` 같은 문자열이며 판매하지 않는 항목은 `"X"`로 내려온다.
- `serviceAreaCode2`는 주유소 코드이며 `restOilList.stdRestCd`와 연결된다.

```json
{
  "count": 226,
  "list": [
    {
      "direction": "부산",
      "pageNo": null,
      "numOfRows": null,
      "routeName": "경부선",
      "serviceAreaCode": "B00001",
      "serviceAreaName": "서울만남(부산)주유소",
      "telNo": "02-573-7430",
      "routeCode": "0010",
      "oilCompany": "AD",
      "lpgYn": "Y",
      "gasolinePrice": "1,999원",
      "diselPrice": "1,997원",
      "lpgPrice": "1,157원",
      "serviceAreaCode2": "000002",
      "svarAddr": "서울시 서초구 원지동10-16"
    }
  ],
  "pageNo": 1,
  "numOfRows": 99,
  "pageSize": 3,
  "message": "인증키가 유효합니다.",
  "code": "SUCCESS"
}
```

#### 코드 기준 처리

- 성공 여부는 `RestOilPriceResponse.isSuccess()`에서 판단한다.
- 원본 행은 `RestOilPriceItem`에 보존하고, 저장 시 문자열 값을 임의 변환하지 않는다.
- `RestOilPriceSyncService`는 페이지 1~3만 호출해 전체 교체 저장한다.
- 페이지 중 하나라도 실패하면 DB 교체 트랜잭션을 실행하지 않아 기존 데이터를 보존한다.
- 빈 응답, 실패 코드와 Feign 예외는 `ExApiClient` 공통 `fetch()`에서 처리한다.

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
