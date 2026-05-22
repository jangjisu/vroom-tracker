#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

if [ -z "${SCOPE:-}" ]; then
  fail_needs_user "plan" "check-scope" \
    "이번 run의 작업 범위가 정해지지 않았습니다." \
    "plan" \
    "SCOPE 값을 backend, frontend, api-integration, git 중 현재 먼저 진행할 범위로 정하세요."
fi

if [ -n "${CATEGORIES:-}" ] && printf '%s' "$CATEGORIES" | grep -q ','; then
  if [ "${CATEGORY_ORDER_CONFIRMED:-false}" != "true" ]; then
    fail_needs_user "plan" "check-scope" \
      "여러 카테고리가 감지되었지만 진행 순서 확인 기록이 없습니다." \
      "plan" \
      "사용자에게 먼저 진행할 카테고리를 확인한 뒤 CATEGORY_ORDER_CONFIRMED=true로 기록하세요."
  fi
fi

pass "plan" "check-scope" "작업 범위가 정해져 있습니다."
