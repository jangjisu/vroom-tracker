#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

violations=""

for file in $(changed_production_java_files); do
  if ! grep -q '@RestController' "$file"; then
    continue
  fi

  class_name=$(basename "$file" .java)

  file_violations=$(
    awk -v class_name="$class_name" '
      /^[[:space:]]*public[[:space:]]+/ &&
      $0 ~ /\)[[:space:]]*(throws[[:space:]]+[A-Za-z0-9_,[:space:]]+)?[[:space:]]*\{/ {
        constructor_pattern = "^[[:space:]]*public[[:space:]]+" class_name "[[:space:]]*\\("
        if ($0 ~ constructor_pattern) {
          next
        }
        if ($0 !~ /^[[:space:]]*public[[:space:]]+ResponseEntity[[:space:]]*<[[:space:]]*ApiResponse[[:space:]]*</) {
          print FILENAME ":" FNR ":" $0
        }
      }
    ' "$file"
  )

  if [ -n "$file_violations" ]; then
    violations="${violations}${file_violations}
"
  fi
done

if [ -n "$violations" ]; then
  printf '%s\n' "$violations" >&2
  fail_autofixable "code" "check-restcontroller-response-type" \
    "@RestController public 메서드 반환 타입이 ResponseEntity<ApiResponse<T>> 형식이 아닙니다." \
    "code" \
    "@RestController public 메서드 반환 타입을 ResponseEntity<ApiResponse<T>>로 통일하세요."
fi

pass "code" "check-restcontroller-response-type" "@RestController 반환 타입 규칙을 만족합니다."
