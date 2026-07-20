# 4단계: 코드 작성

목표: 프로젝트 규칙을 지키면서 코드를 작성하고 필요한 테스트를 함께 작성한다.

코드 작성 단계에서는 다음을 확인한다.

- 변경 파일 범위
- Java/backend 기본 규칙
- 필요한 테스트 파일 존재 여부

이 단계의 hook은 제품 동작을 결정하지 않는다.

대신 다음처럼 기계적으로 확인 가능한 문제를 잡는다.

- `@Data` 사용 여부
- Java `else`, `else if` 사용 여부
- Service/Controller 직접 `new` 생성 사용 여부
- `@RestController` 반환 타입 사용 여부
- Checkstyle/Spotless 등 code quality tool 검사 결과
- Controller/Service/Repository/Scheduler 변경 시 대응 테스트 존재 여부

비즈니스 판단이 필요한 경우에는 자동으로 고치지 않고 사용자 확인이 필요하다.

구현은 승인된 계획을 기준으로 TDD의 Red, Green, Refactor 순서를 따른다.
구현 단위마다 리뷰하지 않고, 전체 구현이 끝난 뒤 코드 게이트를 한 번 실행한다.
