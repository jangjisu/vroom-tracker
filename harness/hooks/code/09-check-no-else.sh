#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

java_files=$(changed_java_files)

if [ -z "$java_files" ]; then
  pass "code" "check-no-else" "변경된 Java 파일이 없습니다."
fi

if ! command -v ast-grep >/dev/null 2>&1; then
  fail_blocked "code" "check-no-else" \
    "Java else 검사를 위한 ast-grep이 설치되어 있지 않습니다." \
    "code" \
    "ast-grep을 설치한 뒤 다시 검사하세요."
fi

rule_file="$ROOT_DIR/harness/config/no-else.yml"

if [ ! -f "$rule_file" ]; then
  fail_blocked "code" "check-no-else" \
    "Java else 검사 규칙 파일이 없습니다." \
    "code" \
    "harness/config/no-else.yml 파일을 복원하세요."
fi

if ! ast-grep scan --rule "$rule_file" $java_files; then
  fail_autofixable "code" "check-no-else" \
    "변경된 Java 파일에서 else 또는 else if가 감지되었습니다." \
    "code" \
    "guard clause, early return, 예외, switch 또는 다형성으로 제어 흐름을 변경하세요."
fi

pass "code" "check-no-else" "변경된 Java 파일에 else와 else if가 없습니다."
