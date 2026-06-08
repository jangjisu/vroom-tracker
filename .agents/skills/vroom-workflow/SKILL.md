---
name: vroom-workflow
description: Use when implementing a feature, bugfix, refactor, API integration, backend change, or frontend behavior change in the vroom-tracker repository.
---

# Vroom Workflow

`vroom-tracker`의 구현 작업을 6단계 하네스와 역할별 리뷰로 진행한다.
설명, 아이디어 상담, 단순 문서 열람과 Git 조회에는 사용하지 않는다.

## 필수 원칙

- `AGENTS.md`와 `harness/WORKFLOW.md`를 먼저 읽는다.
- 하나의 작업 카테고리만 진행한다.
- 설계 승인 전 코드를 수정하지 않는다.
- 리뷰 상태는 결과 파일이 있을 때만 `completed`로 기록한다.
- 각 단계가 실패하면 hook의 `REGRESS_TO` 단계로 돌아간다.

## 1. 계획 생성

**REQUIRED SUB-SKILL:** Use `superpowers:brainstorming`.

영향이 있는 기준 문서만 읽는다.

- 제품 가치나 범위: `PRODUCT.md`
- 데이터 저장과 연결: `DATA.md`, 외부 API는 `API.md`
- 레이어와 책임: `ARCHITECTURE.md`
- 테스트와 품질: `QUALITY.md`

사용자와 요구사항, 대안과 trade-off를 확인한 뒤 계획 초안을 작성한다.

## 2. 스펙 영향도 확인

내부 API, 외부 API, 데이터, 제품, 아키텍처와 품질 영향을 확인한다.
변경이 없더라도 이유를 계획에 기록하고 `harness/harness.sh verify spec`을 통과한다.

## 3. 계획 확정 및 역할 리뷰

코드 변경이 있으면 개발팀장 관점 리뷰를 항상 수행한다.

- **개발팀장:** `/gstack-plan-eng-review`
- **CEO:** 신규 기능, 제품 가치·범위·우선순위 변경 시 `/gstack-plan-ceo-review`
- **DevEx:** API, 라이브러리, 하네스, 개발자 인터페이스 변경 시 `/gstack-plan-devex-review`
- **CSO:** 인증, 키, 외부 입력, 공개 API, 개인정보, 의존성, 배포 변경 시 `/gstack-cso`
- **Design:** 화면 구조나 사용자 흐름 변경 시 `/gstack-plan-design-review`

리뷰 결과를 `harness/runs/current/reviews/`에 저장하고 계획을 수정한다.
전문 리뷰가 불필요하면 계획에 이유를 남기고 상태를 `not-required`로 기록한다.
수정된 계획을 사용자에게 보여주고 승인받은 뒤 `verify review`를 실행한다.

## 4. 코드 작성

**REQUIRED SUB-SKILL:** Use `superpowers:test-driven-development`.
**REQUIRED SUB-SKILL:** Use `superpowers:subagent-driven-development` when sub-agents are available.

계획의 각 구현 단위마다 역할을 분리한다.

1. 구현 에이전트: 실패 테스트 작성, 최소 구현, 테스트 통과
2. 스펙 리뷰 에이전트: 승인된 계획과 API·데이터 계약 준수 확인
3. 품질 리뷰 에이전트: 단순성, 책임 경계, 테스트 의미와 프로젝트 규칙 확인
4. 구현 에이전트: 지적 수정 후 해당 단위 재검증

하위 에이전트를 사용할 수 없으면 같은 순서를 별도 체크포인트로 수행하고 결과를 기록한다.
각 구현 단위가 끝날 때 `harness/harness.sh verify code`를 실행한다.

## 5. 검증 및 Compound

1. `harness/harness.sh verify verify`의 자동 검증을 실행한다.
2. `$ce-code-review mode:agent`로 최종 다중 관점 리뷰를 수행한다.
3. 확실한 지적을 수정하고 전체 검증을 다시 실행한다.
4. 재사용 가능한 문제 해결이나 설계 결정이 있을 때만 `$ce-compound mode:headless`를 실행한다.
5. Compound 결과 또는 생략 이유를 상태 파일과 리뷰 결과에 기록한다.

## 6. Commit / Push

Commit 게이트를 통과한 뒤 작업 브랜치에 커밋하고 push한다.
PR 생성과 기본 브랜치 병합은 사용자가 수행한다.

## 완료 보고

- 계획 및 실행한 역할 리뷰
- 구현 단위별 스펙·품질 리뷰 결과
- 테스트와 커버리지 결과
- Compound 결과 또는 생략 이유
- 브랜치, 커밋과 push 결과
