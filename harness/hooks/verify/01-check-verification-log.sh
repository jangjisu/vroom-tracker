#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if [ -z "${VERIFICATION_LOG:-}" ]; then
  fail_autofixable "verify" "check-verification-log" \
    "검증 결과를 기록할 파일 경로가 없습니다." \
    "verify" \
    "VERIFICATION_LOG 값을 설정하세요."
fi

pass "verify" "check-verification-log" "검증 로그 경로가 설정되어 있습니다."
