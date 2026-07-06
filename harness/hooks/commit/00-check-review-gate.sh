#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/review_gate.sh"

check_review_gate "$1" "commit" "check-review-gate"
pass "commit" "check-review-gate" "review 단계 게이트 통과 상태가 확인되었습니다."
