#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

java_files=$(changed_java_files)

if [ -z "$java_files" ]; then
  pass "code" "check-java-rules" "변경된 Java 파일이 없습니다."
fi

bad_data=$(printf '%s\n' "$java_files" | xargs grep -n '@Data' 2>/dev/null || true)
if [ -n "$bad_data" ]; then
  printf '%s\n' "$bad_data" >&2
  fail_autofixable "code" "check-java-rules" \
    "Lombok @Data 사용이 감지되었습니다." \
    "code" \
    "@Data 대신 필요한 Lombok 어노테이션만 사용하세요."
fi

bad_wildcard=$(printf '%s\n' "$java_files" | xargs grep -nE '^import .*\.\*;' 2>/dev/null || true)
if [ -n "$bad_wildcard" ]; then
  printf '%s\n' "$bad_wildcard" >&2
  fail_autofixable "code" "check-java-rules" \
    "wildcard import가 감지되었습니다." \
    "code" \
    "wildcard import를 실제 사용하는 클래스 import로 바꾸세요."
fi

bad_map=$(printf '%s\n' "$java_files" | xargs grep -nE 'HashMap|Map<String, Object>' 2>/dev/null || true)
if [ -n "$bad_map" ] && [ "${MAP_USAGE_APPROVED:-false}" != "true" ]; then
  printf '%s\n' "$bad_map" >&2
  fail_needs_user "code" "check-java-rules" \
    "Map/HashMap 사용이 감지되었지만 승인 기록이 없습니다." \
    "code" \
    "사용자에게 Map 사용 이유를 설명하고 승인받은 뒤 MAP_USAGE_APPROVED=true로 기록하세요."
fi

pass "code" "check-java-rules" "변경된 Java 파일이 기본 규칙을 만족합니다."
