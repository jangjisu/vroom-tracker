#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

branch=$(git branch --show-current 2>/dev/null || true)

if [ -z "$branch" ]; then
  fail_blocked "commit" "check-branch" \
    "현재 브랜치를 확인할 수 없습니다." \
    "commit" \
    "Git 브랜치 상태를 확인하세요."
fi

case "$branch" in
  main|master)
    fail_needs_user "commit" "check-branch" \
      "기본 브랜치에서 커밋하려고 합니다: $branch" \
      "commit" \
      "규칙에 맞는 새 브랜치를 만들지 사용자 확인이 필요합니다."
    ;;
esac

case "$branch" in
  codex/*)
    pass "commit" "check-branch" "브랜치명이 기본 규칙을 만족합니다."
    ;;
  *)
    fail_needs_user "commit" "check-branch" \
      "브랜치명이 codex/ prefix 규칙과 다릅니다: $branch" \
      "commit" \
      "현재 브랜치를 사용할지, codex/ prefix 브랜치를 만들지 확인하세요."
    ;;
esac
