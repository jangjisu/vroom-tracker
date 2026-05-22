#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

case "${INTERNAL_API_SPEC_IMPACT:-}" in
  changed|unchanged) ;;
  *)
    fail_needs_user "spec" "check-spec-decision" \
      "우리 서버 API 스펙 영향 여부가 정해지지 않았습니다." \
      "spec" \
      "INTERNAL_API_SPEC_IMPACT 값을 changed 또는 unchanged로 정하세요."
    ;;
esac

case "${EXTERNAL_API_SPEC_IMPACT:-}" in
  changed|unchanged|not-applicable) ;;
  *)
    fail_needs_user "spec" "check-spec-decision" \
      "외부 API 연동 스펙 영향 여부가 정해지지 않았습니다." \
      "spec" \
      "EXTERNAL_API_SPEC_IMPACT 값을 changed, unchanged, not-applicable 중 하나로 정하세요."
    ;;
esac

pass "spec" "check-spec-decision" "스펙 영향 여부가 정해져 있습니다."
