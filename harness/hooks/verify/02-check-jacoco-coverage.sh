#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

if ! ./gradlew jacocoTestCoverageVerification --console=plain; then
	fail_autofixable "verify" "check-jacoco-coverage" \
		"JaCoCo 전체 line coverage 95% 기준을 만족하지 못했습니다." \
		"code" \
		"테스트를 보강하거나 불필요한 미사용 코드를 정리한 뒤 다시 검증하세요."
fi

pass "verify" "check-jacoco-coverage" "JaCoCo 전체 line coverage 95% 기준을 만족합니다."
