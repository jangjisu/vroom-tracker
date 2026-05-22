#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if [ -z "${PLAN_FILE:-}" ] || [ ! -f "$PLAN_FILE" ]; then
  fail_autofixable "spec" "check-spec-section" \
    "스펙 영향도 확인을 검사할 계획 문서가 없습니다." \
    "plan" \
    "Plan 단계를 먼저 통과시키세요."
fi

if ! grep -q '스펙 영향도\|Spec Impact' "$PLAN_FILE"; then
  fail_autofixable "spec" "check-spec-section" \
    "계획 문서에 스펙 영향도 확인 섹션이 없습니다." \
    "spec" \
    "계획 문서에 스펙 영향도 확인 섹션을 추가하세요."
fi

pass "spec" "check-spec-section" "스펙 영향도 확인 섹션이 있습니다."
