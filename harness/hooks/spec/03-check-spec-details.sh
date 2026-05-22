#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if [ "${INTERNAL_API_SPEC_IMPACT:-}" = "changed" ]; then
  for word in Endpoint Request Response Error; do
    if ! grep -q "$word" "$PLAN_FILE"; then
      fail_autofixable "spec" "check-spec-details" \
        "내부 API 스펙 변경이 있지만 $word 항목이 없습니다." \
        "spec" \
        "계획 문서의 스펙 영향도 확인 섹션에 $word 항목을 추가하세요."
    fi
  done
fi

if [ "${EXTERNAL_API_SPEC_IMPACT:-}" = "changed" ]; then
  if ! grep -q 'External API\|외부 API\|공공 API' "$PLAN_FILE"; then
    fail_autofixable "spec" "check-spec-details" \
      "외부 API 스펙 변경이 있지만 외부 API 항목이 없습니다." \
      "spec" \
      "계획 문서에 외부 API endpoint/client/response 항목을 추가하세요."
  fi
fi

if [ "${INTERNAL_API_SPEC_IMPACT:-}" = "unchanged" ] && [ "${EXTERNAL_API_SPEC_IMPACT:-}" != "changed" ]; then
  if ! grep -q '변경 없음.*이유\|unchanged.*reason\|판단 이유' "$PLAN_FILE"; then
    fail_autofixable "spec" "check-spec-details" \
      "스펙 변경 없음의 판단 이유가 없습니다." \
      "spec" \
      "계획 문서에 스펙 변경 없음 판단 이유를 추가하세요."
  fi
fi

pass "spec" "check-spec-details" "스펙 영향도 상세가 준비되어 있습니다."
