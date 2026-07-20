#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if [ "${CODE_REVIEW_STATUS:-}" != "completed" ]; then
  fail_autofixable "verify" "check-code-review" \
    "작업 전체의 단일 코드 리뷰가 완료되지 않았습니다." \
    "verify" \
    "코드와 관련 기준 문서를 한 번 리뷰하고 CODE_REVIEW_STATUS=completed로 기록하세요."
fi

if [ "${CODE_REVIEW_COUNT:-}" != "1" ]; then
  fail_autofixable "verify" "check-code-review" \
    "코드 리뷰 실행 횟수가 1회가 아닙니다." \
    "verify" \
    "작업 전체에서 코드 리뷰를 한 번만 실행하고 CODE_REVIEW_COUNT=1로 기록하세요."
fi

if [ -z "${CODE_REVIEW_FILE:-}" ] || [ ! -s "$CODE_REVIEW_FILE" ]; then
  fail_autofixable "verify" "check-code-review" \
    "코드 리뷰 결과 파일이 없습니다." \
    "verify" \
    "단일 리뷰 결과를 harness/runs/current/reviews에 저장하고 CODE_REVIEW_FILE 경로를 기록하세요."
fi

if ! grep -Eiq '^[[:space:]]{0,3}#{1,6}[[:space:]]+(문서 정합성|Documentation consistency)([[:space:]]|$)' "$CODE_REVIEW_FILE"; then
  fail_autofixable "verify" "check-code-review" \
    "코드 리뷰 결과에서 문서 정합성 확인을 찾지 못했습니다." \
    "verify" \
    "코드와 관련 Markdown 기준 문서가 일치하는지 단일 리뷰 결과에 기록하세요."
fi

pass "verify" "check-code-review" "작업 전체의 단일 코드 리뷰와 문서 정합성 확인이 완료되었습니다."
