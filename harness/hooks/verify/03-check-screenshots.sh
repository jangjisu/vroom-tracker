#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"

if [ -z "${SCREENSHOT_DIR:-}" ]; then
  pass "verify" "check-screenshots" "스크린샷 검증이 요구되지 않았습니다."
fi

if [ ! -d "$SCREENSHOT_DIR" ]; then
  fail_autofixable "verify" "check-screenshots" \
    "스크린샷 디렉터리가 없습니다: $SCREENSHOT_DIR" \
    "verify" \
    "화면 검증이 필요한 경우 스크린샷을 생성하세요."
fi

if ! find "$SCREENSHOT_DIR" -type f \( -name '*.png' -o -name '*.jpg' -o -name '*.jpeg' \) | grep -q .; then
  fail_autofixable "verify" "check-screenshots" \
    "스크린샷 이미지가 없습니다: $SCREENSHOT_DIR" \
    "verify" \
    "화면 검증 이미지를 저장하세요."
fi

pass "verify" "check-screenshots" "스크린샷 검증 자료가 있습니다."
