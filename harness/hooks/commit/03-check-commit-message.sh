#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

message_file="${COMMIT_MESSAGE_FILE:-harness/runs/current/commit-message.txt}"

if [ ! -f "$message_file" ]; then
	fail_autofixable "commit" "check-commit-message" \
		"커밋 메시지 초안 파일이 없습니다: $message_file" \
		"commit" \
		"$message_file 파일에 커밋 제목을 작성하세요."
fi

first_line=$(sed -n '1p' "$message_file")

if [ -z "$first_line" ]; then
	fail_autofixable "commit" "check-commit-message" \
		"커밋 메시지 첫 줄이 비어 있습니다." \
		"commit" \
		"커밋 메시지 제목을 작성하세요."
fi

if ! printf '%s\n' "$first_line" | grep -Eq '^(feat|fix|refactor|test|docs|chore|merge)(\([a-z0-9-]+\))?: .+'; then
	fail_autofixable "commit" "check-commit-message" \
		"커밋 메시지 제목 형식이 규칙과 다릅니다: $first_line" \
		"commit" \
		"feat|fix|refactor|test|docs|chore|merge 중 하나로 시작하고, 예: feat: 기능 추가 또는 refactor(service): 로직 정리 형식으로 작성하세요."
fi

pass "commit" "check-commit-message" "커밋 메시지 제목 규칙을 만족합니다."
