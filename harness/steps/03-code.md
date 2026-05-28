# 3단계: 코드 작성

목표: 프로젝트 규칙을 지키면서 코드를 작성하고 필요한 테스트를 함께 작성한다.

코드 작성 단계에서는 다음을 확인한다.

- 변경 파일 범위
- Java/backend 기본 규칙
- 필요한 테스트 파일 존재 여부

이 단계의 hook은 제품 동작을 결정하지 않는다.

대신 다음처럼 기계적으로 확인 가능한 문제를 잡는다.

- `@Data` 사용 여부
- Service/Controller 직접 `new` 생성 사용 여부
- `@RestController` 반환 타입 사용 여부
- Checkstyle/Spotless 등 code quality tool 검사 결과
- Controller/Service/Repository/Scheduler 변경 시 대응 테스트 존재 여부

비즈니스 판단이 필요한 경우에는 자동으로 고치지 않고 사용자 확인이 필요하다.
