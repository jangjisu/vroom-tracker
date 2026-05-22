#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

if [ -z "${WORKFLOW:-}" ]; then
  fail_needs_user "plan" "check-work-type" \
    "작업 유형이 정해지지 않았습니다." \
    "plan" \
    "WORKFLOW 값을 backend-rest-api, public-api-integration, bugfix, frontend-change, fullstack-feature, git-only 중 하나로 정하세요."
fi

pass "plan" "check-work-type" "작업 유형이 정해져 있습니다."
