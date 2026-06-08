#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

if [ "${PLAN_FINALIZED:-false}" != "true" ]; then
  fail_autofixable "review" "check-plan-finalized" \
    "스펙 영향도 확인 결과가 계획에 반영되지 않았습니다." \
    "spec" \
    "계획 문서를 갱신하고 PLAN_FINALIZED=true로 기록하세요."
fi

pass "review" "check-plan-finalized" "스펙 확인 결과가 계획에 반영되었습니다."
