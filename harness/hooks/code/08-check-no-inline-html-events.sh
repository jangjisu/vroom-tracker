#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

changed_html_files=$(changed_files | awk '/^src\/main\/resources\/templates\/.*\.html$/ { print }')

if [ -z "$changed_html_files" ]; then
  pass "code" "check-no-inline-html-events" "변경된 HTML template 파일이 없어 인라인 이벤트 검사를 생략합니다."
fi

violations=$(
  grep -nEi '\son[a-z]+[[:space:]]*=' $changed_html_files 2>/dev/null || true
)

if [ -n "$violations" ]; then
  printf '%s\n' "$violations" >&2
  fail_autofixable "code" "check-no-inline-html-events" \
    "HTML 인라인 이벤트 속성이 감지되었습니다." \
    "code" \
    "onclick/oninput 등 인라인 이벤트 대신 JS에서 addEventListener를 사용하세요."
fi

pass "code" "check-no-inline-html-events" "HTML 인라인 이벤트 속성이 없습니다."
