# 5단계: Commit / Push

목표: 작업 브랜치에 규칙에 맞게 커밋하고 원격 브랜치까지 push할 준비가 되었는지 확인한다.

Commit 단계에서는 다음을 확인한다.

- 기본 브랜치에서 커밋하려는 것은 아닌가
- staged 파일이 존재하는가
- 커밋 메시지 제목이 규칙에 맞는가
- 커밋 후 작업 브랜치를 원격에 push했는가

커밋 메시지 제목은 다음 형식을 사용한다.

```text
type: 내용
type(scope): 내용
```

허용 type은 `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `merge` 이다.

agent의 작업 범위는 작업 브랜치 push까지이다.
PR 생성과 기본 브랜치 병합은 사용자가 직접 수행한다.
