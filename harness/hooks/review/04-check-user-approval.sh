#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

if [ "${PLAN_APPROVED_BY_USER:-false}" != "true" ]; then
  fail_needs_user "review" "check-user-approval" \
    "확정된 계획에 대한 사용자 승인이 없습니다." \
    "review" \
    "사용자에게 최종 계획을 보여주고 승인받은 뒤 PLAN_APPROVED_BY_USER=true로 기록하세요."
fi

pass "review" "check-user-approval" "사용자가 확정된 계획을 승인했습니다."
