#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
. "$ROOT_DIR/harness/lib/result.sh"
. "$ROOT_DIR/harness/lib/state.sh"

load_state "$1"
cd "$(repo_root)"

allowlist="$ROOT_DIR/harness/config/service-controller-new-allowlist.txt"

if [ ! -f "$allowlist" ]; then
	fail_blocked "code" "check-service-controller-new-usage" \
		"Service/Controller new 사용 allowlist 파일이 없습니다." \
		"code" \
		"harness/config/service-controller-new-allowlist.txt 파일을 생성하세요."
fi

target_files=$(changed_production_java_files | awk '/(Service|Controller)\.java$/ { print }')

if [ -z "$target_files" ]; then
	pass "code" "check-service-controller-new-usage" "변경된 Service/Controller Java 파일이 없습니다."
fi

violations=$(awk '
	FNR == NR {
		line = $0
		sub(/#.*/, "", line)
		gsub(/^[ \t]+|[ \t]+$/, "", line)
		if (line != "") {
			allowed[line] = 1
		}
		next
	}

	/^[ \t]*\/\// {
		next
	}

	{
		line = $0
		sub(/\/\/.*/, "", line)

		while (match(line, /(^|[^A-Za-z0-9_])new[ \t]+[A-Z][A-Za-z0-9_]*/)) {
			expr = substr(line, RSTART, RLENGTH)
			sub(/^.*new[ \t]+/, "", expr)

			if (!(expr in allowed)) {
				printf "%s:%d:%s\n", FILENAME, FNR, expr
			}

			line = substr(line, RSTART + RLENGTH)
		}
	}
' "$allowlist" $target_files)

if [ -n "$violations" ]; then
	violation_summary=$(printf '%s\n' "$violations" | awk '
		{
			if (out != "") {
				out = out "; "
			}
			out = out $0
		}
		END {
			print out
		}
	')

	fail_autofixable "code" "check-service-controller-new-usage" \
		"Service/Controller에서 allowlist에 없는 직접 new 생성이 감지되었습니다: $violation_summary" \
		"code" \
		"프로젝트 객체라면 of/from/create 같은 정적 팩토리 메서드로 바꾸고, JDK/라이브러리 예외라면 allowlist에 추가하세요."
fi

pass "code" "check-service-controller-new-usage" "Service/Controller new 사용 규칙을 만족합니다."
