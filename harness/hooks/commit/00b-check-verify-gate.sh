#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/verify_gate.sh"

check_verify_gate "$1" "commit" "check-verify-gate"
pass "commit" "check-verify-gate" "verify 단계 게이트 통과 상태가 확인되었습니다."
