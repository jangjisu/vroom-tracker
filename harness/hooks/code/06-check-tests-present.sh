#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

missing=""

check_test_file() {
  production_file="$1"
  suffix="$2"
  test_file=$(printf '%s\n' "$production_file" |
    sed 's#^src/main/java/#src/test/java/#' |
    sed "s#${suffix}\.java\$#${suffix}Test.java#")

  if [ ! -f "$test_file" ]; then
    missing="${missing}${production_file} -> ${test_file}
"
  fi
}

for file in $(changed_production_java_files); do
  case "$file" in
    *Controller.java) check_test_file "$file" "Controller" ;;
    *Service.java) check_test_file "$file" "Service" ;;
    *Repository.java) check_test_file "$file" "Repository" ;;
    *Scheduler.java) check_test_file "$file" "Scheduler" ;;
  esac
done

if [ -n "$missing" ]; then
  printf '%s\n' "$missing" >&2
  fail_autofixable "code" "check-tests-present" \
    "변경된 production Java 파일에 대응하는 테스트 파일이 없습니다." \
    "code" \
    "대응 테스트를 추가하거나 TODO.md에 대기 항목으로 기록하세요."
fi

pass "code" "check-tests-present" "필요한 테스트 파일이 존재합니다."
