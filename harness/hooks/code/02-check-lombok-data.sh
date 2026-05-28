#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

java_files=$(changed_java_files)

if [ -z "$java_files" ]; then
  pass "code" "check-lombok-data" "변경된 Java 파일이 없습니다."
fi

violations=$(printf '%s\n' "$java_files" | xargs grep -nE '@Data([^A-Za-z0-9_]|$)' 2>/dev/null || true)

if [ -n "$violations" ]; then
  printf '%s\n' "$violations" >&2
  fail_autofixable "code" "check-lombok-data" \
    "Lombok @Data 사용이 감지되었습니다." \
    "code" \
    "@Data 대신 필요한 Lombok 어노테이션만 사용하세요."
fi

pass "code" "check-lombok-data" "변경된 Java 파일에 @Data 사용이 없습니다."
