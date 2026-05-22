#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

if [ -z "${PLAN_FILE:-}" ]; then
  fail_autofixable "plan" "check-plan-doc" \
    "계획 문서 경로가 state에 없습니다." \
    "plan" \
    "PLAN_FILE 값을 설정하고 계획 문서를 생성하세요."
fi

cd "$(repo_root)"

if [ ! -f "$PLAN_FILE" ]; then
  fail_autofixable "plan" "check-plan-doc" \
    "계획 문서가 존재하지 않습니다: $PLAN_FILE" \
    "plan" \
    "계획 문서를 생성하세요."
fi

if ! grep -q '^## .*목표\|^## .*Goal\|^## .*작업' "$PLAN_FILE"; then
  fail_autofixable "plan" "check-plan-doc" \
    "계획 문서에 작업 목표 섹션이 없습니다." \
    "plan" \
    "계획 문서에 작업 목표 섹션을 추가하세요."
fi

pass "plan" "check-plan-doc" "계획 문서가 준비되어 있습니다."
