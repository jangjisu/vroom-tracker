# RestRoute

이동 경로에 있는 고속도로 휴게소를 찾고, 위치·시설·먹거리·주유 정보를 비교하는 Spring Boot 애플리케이션입니다.

## 현재 기능

### 지도와 휴게소 상세

- 네이버 지도에 전국 휴게소 위치와 현재 위치를 표시합니다.
- 휴게소 마커를 선택하면 노선, 방향, 주소, 편의시설, 운영 상태와 주차 정보를 보여줍니다.
- 주유 가격, 정유사, 전화번호와 주유소 편의시설을 상세 정보에 포함합니다.
- 대표 먹거리와 전체 메뉴를 모달에서 확인할 수 있습니다.
- 주유 가격은 사용자가 단건 갱신할 수 있으며, 최근 10분 이내 갱신된 값은 외부 API를 다시 호출하지 않습니다.

### 경로상 휴게소 탐색

- 현재 위치와 사용자가 선택한 목적지를 기준으로 카카오 자동차 경로를 조회합니다.
- 목적지 검색 결과가 여러 개면 이름과 주소를 보여주고 사용자가 직접 선택하게 합니다.
- 경로와 출발·도착 마커를 지도에 표시합니다.
- 기본 1km 범위 안의 휴게소를 경로 순서대로 보여주고, 목록에서 상세 정보로 이동할 수 있습니다.

### 데이터 동기화

- 서버 시작 시 휴게소 위치, 상세, 주유소 편의시설, 주유 가격과 먹거리 테이블이 비어 있으면 초기 데이터를 수집합니다.
- 휴게소 위치·상세·영업시설·주유소 편의시설·먹거리는 매일 자정에 갱신합니다.
- 주유 가격은 3시간마다 갱신합니다.
- 외부 API 호출이 실패하면 항목별 오류를 로그에 남기고 다른 동기화 작업을 계속합니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Spring Boot 3.5.0, Java 17 |
| HTTP Client | Spring Cloud OpenFeign |
| Database | H2 file DB, Spring Data JPA |
| Frontend | Thymeleaf, Bootstrap 5, Vanilla JavaScript |
| Map | Naver Maps JavaScript API |
| Place and Route | Kakao Local API, Kakao Mobility Directions API |
| Test | JUnit 5, Spring Boot Test, Node.js test runner, JaCoCo |
| Quality | Checkstyle, Spotless, Palantir Java Format, ESLint, SonarQube |

## 로컬 실행

### 1. API 키 설정

다음 환경 변수를 설정합니다.

```bash
export EX_API_KEY=YOUR_EX_API_KEY
export NAVER_MAPS_NCP_KEY_ID=YOUR_NAVER_MAPS_NCP_KEY_ID
export KAKAO_REST_API_KEY=YOUR_KAKAO_REST_API_KEY
```

한국도로공사 키는 Git에서 제외된 `src/main/resources/application-local.properties`에도 설정할 수 있습니다.

```properties
ex.api.key=YOUR_EX_API_KEY
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080`에 접속합니다. 로컬 데이터는 `./data/rest-route` H2 파일에 유지됩니다.

## 주요 내부 API

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/map-config` | 네이버 지도 클라이언트 키 설정 조회 |
| `GET` | `/api/rest-stops` | 저장된 휴게소 목록 조회 |
| `GET` | `/api/rest-stops/{serviceAreaCode}` | 휴게소 상세·주유·먹거리 조회 |
| `POST` | `/api/rest-stops/{serviceAreaCode}/oil-price/refresh` | 휴게소 주유 가격 단건 갱신 |
| `GET` | `/api/place-search?query=...` | 목적지 후보 목록 조회 |
| `GET` | `/api/route-rest-stops` | 경로와 경로상 휴게소 조회 |

요청과 응답, 외부 API 연결 상세는 `API.md`를 참고합니다.

## 검증

백엔드 테스트와 품질 검사를 실행합니다.

```bash
./gradlew check
```

프론트엔드 검사를 실행합니다.

```bash
npm ci
npm run lint
npm run test:js
```

전체 JaCoCo line coverage 최소 기준은 95%이며, 하네스는 변경 실행 라인과 변경 조건 분기의 100% 검증도 확인합니다.

## 주요 문서

| 문서 | 역할 |
|---|---|
| `PRODUCT.md` | 제품 범위와 합의된 방향 |
| `USER_INSIGHTS.md` | 사용자 유형, 행동 가설과 브레인스토밍 질문 |
| `DATA.md` | 외부 데이터 저장 방식과 테이블 연결 키 |
| `ARCHITECTURE.md` | 레이어 책임과 제어 흐름 원칙 |
| `API.md` | 외부 연동 실측과 내부 API 계약 |
| `QUALITY.md` | 테스트와 품질 기준 |
