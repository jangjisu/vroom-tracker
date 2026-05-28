# Entity / DTO 수정 시 확인 규칙

## Entity

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "...")
public class XxxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ...

    public static XxxEntity from(XxxItem item) { ... }
}
```

- 외부 API Item → Entity 변환은 `from()` 정적 팩토리로 처리한다
- 서비스에서 빌더를 직접 호출하지 않는다 (테스트 데이터 구성 목적은 허용)

## DTO

- 외부 API 응답 클래스(VO)와 내부 DTO는 반드시 분리한다
- 내부 DTO 필드명에 외부 API 약어가 그대로 남아 있으면 안 된다

```java
// ❌ API 원시 필드명 그대로 복사
private final String sphlDfttNm;

// ✅ from() 에서 이름 변환
public static XxxDto from(XxxEntity entity) {
    return XxxDto.builder()
            .dayType(entity.getSphlDfttNm())
            .build();
}
```

## API 응답 → 내부 객체 변환 규칙

- 변환 로직은 `from()` / `of()` 내부에서 처리한다
- 컬렉션 수준 계산(rank, 집계 합산 등)은 서비스에서 계산 후 파라미터로 전달한다
- 서비스는 "언제 변환할지"와 "컬렉션 수준 계산"만 담당한다
