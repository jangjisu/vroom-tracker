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

case "${COMPOUND_KNOWLEDGE_STATUS:-}" in
  completed)
    if [ -z "${COMPOUND_KNOWLEDGE_FILE:-}" ] || [ ! -s "$COMPOUND_KNOWLEDGE_FILE" ]; then
      fail_autofixable "verify" "check-compound-review" \
        "Compound 학습 기록 파일이 없습니다." \
        "verify" \
        "ce-compound 결과 파일 경로를 COMPOUND_KNOWLEDGE_FILE에 기록하세요."
    fi
    ;;
  not-required) ;;
  *)
    fail_autofixable "verify" "check-compound-review" \
      "Compound 학습 기록 여부가 결정되지 않았습니다." \
      "verify" \
      "재사용할 학습이 있으면 ce-compound를 수행하고, 없으면 not-required로 기록하세요."
    ;;
esac

pass "verify" "check-compound-review" "Compound 최종 리뷰와 학습 기록 판단이 완료되었습니다."
