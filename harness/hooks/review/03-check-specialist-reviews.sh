#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

check_review_status() {
  impact="$1"
  status="$2"
  review_name="$3"

  case "$impact" in
    true)
      if [ "$status" != "completed" ]; then
        fail_autofixable "review" "check-specialist-reviews" \
          "$review_name 영향이 있지만 리뷰 완료 기록이 없습니다." \
          "review" \
          "$review_name 리뷰를 수행하고 상태를 completed로 기록하세요."
      fi
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

check_review_status "${DX_IMPACT:-}" "${DEVEX_REVIEW_STATUS:-}" "개발자 경험"
check_review_status "${SECURITY_IMPACT:-}" "${SECURITY_REVIEW_STATUS:-}" "보안"

pass "review" "check-specialist-reviews" "필요한 전문 리뷰 상태가 기록되었습니다."
