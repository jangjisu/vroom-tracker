#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

java_files=$(changed_production_java_files)

if [ -z "$java_files" ]; then
  pass "verify" "check-changed-code-coverage" "변경된 production Java 파일이 없습니다."
fi

report_file="build/reports/jacoco/test/jacocoTestReport.xml"

if [ ! -f "$report_file" ]; then
  fail_blocked "verify" "check-changed-code-coverage" \
    "JaCoCo XML 보고서가 없습니다." \
    "verify" \
    "./gradlew test jacocoTestReport를 실행한 뒤 다시 검사하세요."
fi

changed_lines=$(mktemp)
report_lines=$(mktemp)
trap 'rm -f "$changed_lines" "$report_lines"' EXIT HUP INT TERM

git diff --unified=0 --no-color HEAD -- src/main/java |
  awk '
    /^\+\+\+ b\// {
      file = $0
      sub(/^\+\+\+ b\//, "", file)
      next
    }
    /^@@ / {
      split($0, parts, " ")
      added = parts[3]
      sub(/^\+/, "", added)
      split(added, range, ",")
      start = range[1] + 0
      count = range[2] == "" ? 1 : range[2] + 0
      for (line = start; line < start + count; line++) {
        print file ":" line
      }
    }
  ' >"$changed_lines"

for file in $java_files; do
  if ! git ls-files --error-unmatch "$file" >/dev/null 2>&1; then
    awk -v file="$file" '{ print file ":" FNR }' "$file" >>"$changed_lines"
  fi
done

sort -u "$changed_lines" -o "$changed_lines"
sed 's/></>\
</g' "$report_file" >"$report_lines"

set +e
coverage_result=$(
  awk '
    function attribute(text, name, value) {
      value = text
      sub(".*" name "=\"", "", value)
      sub("\".*", "", value)
      return value
    }

    FNR == NR {
      changed[$0] = 1
      next
    }

    /^<package name=/ {
      package_name = attribute($0, "name")
      next
    }

    /^<sourcefile name=/ {
      source_name = attribute($0, "name")
      source_path = "src/main/java/"
      if (package_name != "") {
        source_path = source_path package_name "/"
      }
      source_path = source_path source_name
      next
    }

    /^<\/sourcefile>/ {
      source_name = ""
      source_path = ""
      next
    }

    /^<line / && source_path != "" {
      line_number = attribute($0, "nr")
      key = source_path ":" line_number
      if (!(key in changed)) {
        next
      }

      matched++
      missed_instructions = attribute($0, "mi") + 0
      missed_branches = attribute($0, "mb") + 0

      if (missed_instructions > 0) {
        print key ": 변경 실행 라인이 테스트에서 실행되지 않았습니다."
        failed = 1
      }

      if (missed_branches > 0) {
        print key ": 변경 조건의 일부 분기가 테스트되지 않았습니다."
        failed = 1
      }
    }

    END {
      if (matched == 0) {
        exit 0
      }
      if (failed) {
        exit 1
      }
    }
  ' "$changed_lines" "$report_lines"
)
coverage_exit=$?
set -e

case "$coverage_exit" in
  0)
    pass "verify" "check-changed-code-coverage" \
      "변경 실행 라인과 변경 조건 분기가 모두 테스트되었거나 실행 코드 변경이 없습니다."
    ;;
  1)
    printf '%s\n' "$coverage_result" >&2
    fail_autofixable "verify" "check-changed-code-coverage" \
      "변경 코드의 라인 또는 분기 커버리지가 100%가 아닙니다." \
      "code" \
      "표시된 실행 경로를 검증하는 의미 있는 테스트를 추가하세요."
    ;;
  *)
    printf '%s\n' "$coverage_result" >&2
    fail_blocked "verify" "check-changed-code-coverage" \
      "변경 코드와 JaCoCo 보고서를 연결하지 못했습니다." \
      "verify" \
      "JaCoCo XML 보고서와 변경 파일 범위를 확인하세요."
    ;;
esac
