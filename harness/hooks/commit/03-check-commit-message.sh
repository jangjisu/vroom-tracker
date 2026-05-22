#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if [ -z "${COMMIT_MESSAGE_FILE:-}" ]; then
  fail_autofixable "commit" "check-commit-message" \
    "커밋 메시지 파일 경로가 없습니다." \
    "commit" \
    "COMMIT_MESSAGE_FILE 값을 설정하고 커밋 메시지 초안을 작성하세요."
fi

if [ ! -f "$COMMIT_MESSAGE_FILE" ]; then
  fail_autofixable "commit" "check-commit-message" \
    "커밋 메시지 파일이 없습니다: $COMMIT_MESSAGE_FILE" \
    "commit" \
    "커밋 메시지 파일을 생성하세요."
fi

first_line=$(sed -n '1p' "$COMMIT_MESSAGE_FILE")

if [ -z "$first_line" ]; then
  fail_autofixable "commit" "check-commit-message" \
    "커밋 메시지 첫 줄이 비어 있습니다." \
    "commit" \
    "커밋 메시지 제목을 작성하세요."
fi

pass "commit" "check-commit-message" "커밋 메시지 초안이 있습니다."
