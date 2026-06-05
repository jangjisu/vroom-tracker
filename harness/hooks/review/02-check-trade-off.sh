#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if [ "${TRADE_OFF_REVIEWED:-false}" != "true" ]; then
  fail_autofixable "review" "check-trade-off" \
    "접근 방법과 trade-off 검토가 완료되지 않았습니다." \
    "review" \
    "선택한 방법, 대안과 선택 이유를 계획에 기록하세요."
fi

if [ -z "${PLAN_FILE:-}" ] || [ ! -f "$PLAN_FILE" ]; then
  fail_autofixable "review" "check-trade-off" \
    "검토할 계획 문서가 없습니다." \
    "plan" \
    "Plan 단계를 먼저 통과시키세요."
fi

if ! grep -Eiq 'trade[- ]?off|대안|선택 이유' "$PLAN_FILE"; then
  fail_autofixable "review" "check-trade-off" \
    "계획 문서에서 접근 방법의 비교 근거를 찾지 못했습니다." \
    "review" \
    "계획 문서에 대안과 선택 이유를 기록하세요."
fi

pass "review" "check-trade-off" "접근 방법과 trade-off가 검토되었습니다."
