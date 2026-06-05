# vroom-tracker

고속도로 관련 공공 데이터를 활용하기 위한 Spring Boot 기반 백엔드 프로젝트입니다.

현재 앱은 첫 화면을 서빙하는 기본 Controller와 한국도로공사 공공데이터 Feign Client, 공통 API 응답 구조를 중심으로 구성되어 있습니다.

---

## 현재 기능

- `GET /`
  - `index.html` 화면을 반환합니다.
  - 현재 화면은 기본 네비게이션과 빈 컨테이너만 포함합니다.
- 한국도로공사 휴게소 위치 API Client
  - `/openapi/locationinfo/locationinfoRest`
  - 응답 VO: `RestStopResponse`, `RestStopItem`
- 공통 응답 구조
  - `ApiResponse<T>`
  - `ResponseCode`
  - `GlobalExceptionHandler`

---

## 기술 스택

| 항목 | 기술 |
|---|---|
| Backend | Spring Boot 3.5.0, Java 17 |
| HTTP Client | Spring Cloud OpenFeign |
| DB | H2, Spring Data JPA |
| Frontend | Thymeleaf, Bootstrap 5, Vanilla JS |
| Test | JUnit 5, Spring Boot Test, JaCoCo |
| Quality | Checkstyle, Spotless, Palantir Java Format |

---

## 로컬 실행

### 1. API 키 설정

`src/main/resources/application-local.properties` 파일에 실제 API 키를 설정합니다.

```properties
ex.api.key=YOUR_API_KEY_HERE
```

또는 환경 변수로 설정할 수 있습니다.

```bash
export EX_API_KEY=YOUR_API_KEY_HERE
```

### 2. 실행

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080`에 접속합니다.

---

## 검증

전체 테스트와 품질 검사를 실행합니다.

```bash
./gradlew check
```

현재 `check`에는 다음 검증이 포함됩니다.

- Checkstyle
- Spotless Java format check
- JUnit test
- JaCoCo line coverage verification

JaCoCo 전체 line coverage 최소 기준은 `95%`입니다.
하네스 검증에서는 신규·변경 실행 라인과 변경 조건 분기의 `100%` 커버리지도 확인합니다.

---

## 주요 구조

```text
src/main/java/com/vroomtracker/
├── VroomTrackerApplication.java
├── client/
│   ├── ExApiClient.java
│   └── response/
│       ├── RestStopItem.java
│       └── RestStopResponse.java
├── common/
│   ├── ApiResponse.java
│   ├── GlobalExceptionHandler.java
│   └── ResponseCode.java
└── controller/
    └── TrafficController.java
```

```text
src/main/resources/
├── application.properties
├── static/
│   ├── css/style.css
│   └── js/
│       ├── app.js
│       └── utils.js
└── templates/
    └── index.html
```
