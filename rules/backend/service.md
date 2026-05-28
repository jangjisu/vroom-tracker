# Service 수정 시 확인 규칙

## 레이어 경계

- Service A는 Service B 소유의 Repository에 직접 접근하지 않는다
- 두 Service가 서로를 참조하는 순환 의존은 금지
- Service 간 호출은 한 방향으로만 흐른다 (A → B 허용, A ↔ B 금지)

## @Transactional 범위

`@Transactional` 범위 안에서 외부 API 호출·파일 I/O 등 느린 작업을 포함하지 않는다.

- fetch (외부 API 호출) → 트랜잭션 밖
- DB 저장/수정 → 트랜잭션 안

```java
// ✅ 올바른 분리
public void refresh() {
    List<XxxItem> items = fetchFromApi();  // 트랜잭션 밖
    save(items);                           // 트랜잭션 안
}

@Transactional
void save(List<XxxItem> items) {
    repository.deleteAll();
    repository.saveAll(...);
}
```
