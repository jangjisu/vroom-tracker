# Test Properties 분리 & Unused Import 자동 정리 구현 계획

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `VroomTrackerApplicationTests`의 인라인 `@TestPropertySource` 설정을 `application-test.properties` 파일로 분리하고, 테스트 실행 시 사용하지 않는 import를 제거하는 규칙을 CLAUDE.md에 추가한다.

**Architecture:**
- `src/test/resources/application-test.properties` 생성 후 `@ActiveProfiles("test")`로 로드
- `@TestPropertySource` 인라인 속성 제거 → Spring 표준 테스트 프로파일 방식으로 전환
- CLAUDE.md에 unused import 제거 규칙 추가 (별도 코드 변경 없음)

**Tech Stack:** Spring Boot Test, JUnit 5, H2 in-memory

---

## Task 1: 테스트 프로파일 프로퍼티 파일 생성

**Files:**
- Create: `src/test/resources/application-test.properties`
- Modify: `src/test/java/com/vroomtracker/VroomTrackerApplicationTests.java`

### 현재 상태

`VroomTrackerApplicationTests`는 `@TestPropertySource`로 속성을 인라인 정의한다.

```java
@SpringBootTest
@TestPropertySource(properties = {
        "ex.api.url=http://localhost",
        "ex.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
```

### 문제점

- 테스트 공통 설정이 특정 클래스에 묶여 있어 다른 테스트 클래스가 같은 설정을 재정의해야 함
- 인라인 속성은 IDE 자동완성·검색 불가
- 설정이 늘어날수록 테스트 클래스 상단이 오염됨

- [ ] **Step 1: `src/test/resources/` 디렉터리 생성 확인 후 `application-test.properties` 작성**

```properties
# Feign: prevent real API calls during tests
ex.api.url=http://localhost
ex.api.key=test-key

# Use in-memory H2 for tests (avoids file-based H2 dialect issues)
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

- [ ] **Step 2: `VroomTrackerApplicationTests` 어노테이션 교체**

`@TestPropertySource` → `@ActiveProfiles("test")` 로 변경.
`@SpringBootTest`는 `src/test/resources/application-test.properties`를 자동으로 로드한다.

```java
@SpringBootTest
@ActiveProfiles("test")
class VroomTrackerApplicationTests {

    @Test
    @DisplayName("Spring Application Context 가 정상적으로 로드된다")
    void contextLoads() {
    }
}
```

import 변경:
- 제거: `import org.springframework.test.context.TestPropertySource;`
- 추가: `import org.springframework.test.context.ActiveProfiles;`

- [ ] **Step 3: 테스트 실행**

```bash
./gradlew test
```

Expected: `BUILD SUCCESSFUL`, 전체 통과

- [ ] **Step 4: 완료 처리**

`TODO.md`에서 해당 항목을 `[x]`로 변경 후 완료 섹션으로 이동.

---

## Task 2: CLAUDE.md에 unused import 제거 규칙 추가

**Files:**
- Modify: `CLAUDE.md`

### 추가할 규칙

테스트를 실행할 때마다(또는 코드를 수정할 때마다) 변경한 파일에 사용하지 않는 import가 있다면 즉시 제거한다.

- [ ] **Step 1: CLAUDE.md `코드 수정 후 필수` 섹션 아래에 규칙 추가**

```markdown
## 사용하지 않는 import 제거

코드를 수정하거나 테스트를 실행한 뒤, **변경한 파일**에 사용하지 않는 import가 있으면 즉시 제거한다.
IDE 경고나 컴파일러 경고에 관계없이, 수정 범위 내 파일은 항상 점검한다.

점검 기준:
- 추가·수정·삭제 작업이 발생한 `.java` 파일 대상
- import 구문이 해당 파일 내 어디서도 참조되지 않으면 제거
- wildcard import (`import java.util.*`) 는 실제 사용 클래스로 교체

```bash
# IntelliJ: Ctrl+Alt+O (Mac: ⌃⌥O) 또는 Optimize Imports
# 또는 수동으로 미사용 import 줄 삭제 후 테스트 재실행
```
```

- [ ] **Step 2: 테스트 실행하여 변경 사항 영향 없음 확인**

```bash
./gradlew test
```

Expected: `BUILD SUCCESSFUL`
