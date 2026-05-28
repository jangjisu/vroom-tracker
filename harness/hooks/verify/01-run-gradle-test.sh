#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if ! ./gradlew cleanTest test --console=plain; then
	fail_autofixable "verify" "run-gradle-test" \
		"전체 테스트가 실패했습니다." \
		"code" \
		"테스트 실패 원인을 분석한 뒤 코드 작성 단계로 회귀하세요."
fi

pass "verify" "run-gradle-test" "전체 테스트가 통과했습니다."
