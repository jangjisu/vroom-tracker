# 하네스

이 하네스는 하나의 작업을 다음 6단계 게이트로 나누어 진행한다.

1. 계획 생성
2. 스펙 영향도 확인
3. 계획 확정 및 리뷰
4. 코드 작성
5. 검증
6. Commit / Push

각 게이트는 안정적인 큰 단계이고, 각 단계 안에는 통과 조건을 검사하는 작은 shell hook들이 있다.

hook은 다음 단계로 넘어가도 되는지 판단하는 역할을 한다.

다만 포맷처럼 기계적으로 복구 가능한 항목은 hook이 자동 적용할 수 있다.
그 외 코드 의미를 바꾸는 수정은 hook이 직접 처리하지 않고 실패로 판단한다.

## 사용법

먼저 현재 작업 run의 상태 파일을 만든다.

```bash
cp harness/runs/current/state.env.example harness/runs/current/state.env
```

그 다음 원하는 단계를 검증한다.

```bash
harness/harness.sh list
harness/harness.sh verify plan
harness/harness.sh verify spec
harness/harness.sh verify review
harness/harness.sh verify code
harness/harness.sh verify verify
harness/harness.sh verify commit
```

전체 단계를 순서대로 검증하려면 다음 명령을 사용한다.

```bash
harness/harness.sh verify all
```

## Hook 종료 코드

| Exit | Meaning |
|---:|---|
| `0` | 통과 |
| `10` | 실패했지만 보통 하네스/AI가 자동으로 보완할 수 있음 |
| `20` | 실패했고 사용자 확인이 필요함 |
| `30` | 차단됨 |
| `99` | 알 수 없는 hook 실패 |

hook은 상태 파일과 실제 파일을 검사하고, 표준화된 결과를 출력한다.

기본 원칙은 hook이 검사자 역할을 한다는 것이다.
자동 포맷처럼 안전한 경우만 hook에서 처리하고, 보완 작업은 사용자 또는 AI가 수행한 뒤 같은 단계를 다시 검증한다.
