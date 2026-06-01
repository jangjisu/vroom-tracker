#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

changed_frontend_files=$(
  changed_files |
    awk '
      /^src\/main\/resources\/static\/js\/.*\.js$/ { print; next }
      /^package\.json$/ { print; next }
      /^package-lock\.json$/ { print; next }
      /^eslint\.config\.(js|mjs|cjs)$/ { print; next }
    '
)

if [ -z "$changed_frontend_files" ]; then
  pass "code" "run-eslint" "변경된 JS/ESLint 설정 파일이 없어 ESLint 실행을 생략합니다."
fi

if [ ! -f package.json ]; then
  fail_blocked "code" "run-eslint" \
    "package.json이 없어 ESLint를 실행할 수 없습니다." \
    "code" \
    "package.json과 ESLint 설정을 추가하세요."
fi

if ! npm run lint; then
  fail_autofixable "code" "run-eslint" \
    "ESLint 검사에서 문제가 감지되었습니다." \
    "code" \
    "ESLint 결과를 확인하고 JS 문법/규칙 위반을 수정하세요."
fi

pass "code" "run-eslint" "ESLint 검사가 통과했습니다."
