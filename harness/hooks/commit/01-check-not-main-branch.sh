#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

branch=$(git branch --show-current 2>/dev/null || true)

if [ -z "$branch" ]; then
	fail_blocked "commit" "check-not-main-branch" \
		"현재 브랜치를 확인할 수 없습니다." \
		"commit" \
		"Git 브랜치 상태를 확인하세요."
fi

case "$branch" in
	main | master)
		fail_needs_user "commit" "check-not-main-branch" \
			"기본 브랜치에서 커밋하려고 합니다: $branch" \
			"commit" \
			"작업 브랜치를 만든 뒤 커밋하세요."
		;;
esac

pass "commit" "check-not-main-branch" "기본 브랜치가 아니므로 커밋할 수 있습니다."
