#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if [ "${COMPOUND_CODE_REVIEW_STATUS:-}" != "completed" ]; then
  fail_autofixable "verify" "check-compound-review" \
    "Compound Engineering 최종 코드 리뷰가 완료되지 않았습니다." \
    "verify" \
    "ce-code-review를 수행하고 COMPOUND_CODE_REVIEW_STATUS=completed로 기록하세요."
fi

if [ -z "${COMPOUND_CODE_REVIEW_FILE:-}" ] || [ ! -s "$COMPOUND_CODE_REVIEW_FILE" ]; then
  fail_autofixable "verify" "check-compound-review" \
    "Compound Engineering 코드 리뷰 결과 파일이 없습니다." \
    "verify" \
    "리뷰 결과를 harness/runs/current/reviews에 저장하고 COMPOUND_CODE_REVIEW_FILE 경로를 기록하세요."
fi

pass "verify" "check-compound-review" "Compound 최종 코드 리뷰가 완료되었습니다."
