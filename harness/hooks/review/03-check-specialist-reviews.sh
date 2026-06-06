#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

check_completed_review() {
  status="$1"
  review_file="$2"
  review_name="$3"

  if [ "$status" != "completed" ]; then
    fail_autofixable "review" "check-specialist-reviews" \
      "$review_name 리뷰 완료 기록이 없습니다." \
      "review" \
      "$review_name 리뷰를 수행하고 상태를 completed로 기록하세요."
  fi

  if [ -z "$review_file" ] || [ ! -s "$review_file" ]; then
    fail_autofixable "review" "check-specialist-reviews" \
      "$review_name 리뷰 결과 파일이 없습니다." \
      "review" \
      "$review_name 리뷰 결과를 harness/runs/current/reviews에 저장하고 파일 경로를 기록하세요."
  fi
}

check_review_status() {
  impact="$1"
  status="$2"
  review_file="$3"
  review_name="$4"

  case "$impact" in
    true)
      check_completed_review "$status" "$review_file" "$review_name"
      ;;
    false)
      if [ "$status" != "not-required" ]; then
        fail_autofixable "review" "check-specialist-reviews" \
          "$review_name이 필요하지 않다는 판단 기록이 없습니다." \
          "review" \
          "$review_name 상태를 not-required로 기록하고 계획에 이유를 남기세요."
      fi
      ;;
    *)
      fail_needs_user "review" "check-specialist-reviews" \
        "$review_name 영향 여부가 정해지지 않았습니다." \
        "review" \
        "영향 여부를 true 또는 false로 정하세요."
      ;;
  esac
}

check_completed_review \
  "${PLAN_ENG_REVIEW_STATUS:-}" \
  "${PLAN_ENG_REVIEW_FILE:-}" \
  "개발팀장 관점"

check_review_status \
  "${PRODUCT_IMPACT:-}" \
  "${CEO_REVIEW_STATUS:-}" \
  "${CEO_REVIEW_FILE:-}" \
  "CEO 관점"

check_review_status \
  "${DX_IMPACT:-}" \
  "${DEVEX_REVIEW_STATUS:-}" \
  "${DEVEX_REVIEW_FILE:-}" \
  "개발자 경험"

check_review_status \
  "${SECURITY_IMPACT:-}" \
  "${SECURITY_REVIEW_STATUS:-}" \
  "${SECURITY_REVIEW_FILE:-}" \
  "보안"

check_review_status \
  "${DESIGN_IMPACT:-}" \
  "${DESIGN_REVIEW_STATUS:-}" \
  "${DESIGN_REVIEW_FILE:-}" \
  "Design"

pass "review" "check-specialist-reviews" "역할별 계획 리뷰 결과와 생략 판단이 확인되었습니다."
