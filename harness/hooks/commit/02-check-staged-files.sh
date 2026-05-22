#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

staged=$(git diff --cached --name-only --diff-filter=ACMRTUXB)

if [ -z "$staged" ]; then
  fail_autofixable "commit" "check-staged-files" \
    "staged 파일이 없습니다." \
    "commit" \
    "커밋할 파일을 계획 범위에 맞게 stage 하세요."
fi

pass "commit" "check-staged-files" "staged 파일이 있습니다."
