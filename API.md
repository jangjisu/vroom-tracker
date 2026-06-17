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

---

# 카카오 API 연동 (경로 탐색)

한국도로공사 OpenAPI와 별개 제공자(Kakao). 경로 기반 휴게소 탐색용. 인증은 헤더 `Authorization: KakaoAK {REST_API_KEY}`. 키는 서버 환경변수로만 주입하고 소스/커밋에 넣지 않는다.

요금: 카카오모빌리티 자동차 길찾기는 **일 10,000건 무료**(초과분만 과금). 로컬(주소/장소 검색)은 무료 쿼터.

## 자동차 길찾기 — Directions (카카오모빌리티)

### 엔드포인트
```
GET https://apis-navi.kakaomobility.com/v1/directions
```

### 요청 파라미터 (2026-06-17 실측)
- `origin` (필수): `경도,위도` (예: `127.039,37.484`)
- `destination` (필수): `경도,위도`
- `priority` (선택): `RECOMMEND` | `TIME` | `DISTANCE`

### 응답 (2026-06-17 실측)
- 최상위: `trans_id`, `routes`
- `routes[0].result_code` = `0` 이 성공(`result_msg`="길찾기 성공"). 실패 시 0 외 코드.
- `routes[0].summary.distance`(m), `summary.duration`(초), `summary.origin/destination`
- `routes[0].sections[].roads[].vertexes` = **[경도,위도,경도,위도,...] 평탄 배열**(경로 폴리라인). 서울→부산 실측 시 약 3,012개 좌표.

### 실측 샘플 (서울→부산)
```
HTTP 200, result_code 0, distance 382296m, duration 16572s, sections 1, path points ~3012
vertexes 예: [127.03902,37.48391, 127.03877,37.48387, ...]
```

## 주소/장소 검색 — Local (Kakao Developers)

콘솔에서 카카오맵(OPEN_MAP_AND_LOCAL) 서비스 활성화 필요(무료). 미활성 시 403 `NotAuthorizedError`.

### 엔드포인트 (2026-06-17 실측)
```
GET https://dapi.kakao.com/v2/local/search/keyword.json?query={장소}   # 장소명
GET https://dapi.kakao.com/v2/local/search/address.json?query={주소}   # 주소
```

### 응답 (2026-06-17 실측)
- `documents[]` 배열. 각 항목 `x`(경도, 문자열), `y`(위도, 문자열), `place_name`(키워드) / `address_name`·`road_address_name`(주소).
- 실측: `해운대해수욕장`(keyword) → x=129.1598, y=35.1585 / `부산 해운대구 우동`(address) → x=129.1484, y=35.1727.
