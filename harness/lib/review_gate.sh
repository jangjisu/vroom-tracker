#!/usr/bin/env sh

check_review_gate() {
  state_file="$1"
  step="$2"
  hook_name="$3"

  review_dir="$ROOT_DIR/harness/hooks/review"

  for review_hook in "$review_dir"/*.sh; do
    [ -f "$review_hook" ] || continue

    set +e
    output=$("$review_hook" "$state_file")
    code=$?
    set -e

    if [ "$code" -eq 0 ]; then
      continue
    fi

    message=$(printf '%s\n' "$output" | awk -F= '/^MESSAGE=/ { print substr($0, 9); exit }')
    [ -n "$message" ] || message="review 단계 게이트가 완료되지 않았습니다."

    case "$code" in
      20)
        fail_needs_user "$step" "$hook_name" "$message" "review" \
          "먼저 harness/harness.sh verify review를 통과하세요."
        ;;
      30)
        fail_blocked "$step" "$hook_name" "$message" "review" \
          "먼저 harness/harness.sh verify review를 통과하세요."
        ;;
      *)
        fail_autofixable "$step" "$hook_name" "$message" "review" \
          "먼저 harness/harness.sh verify review를 통과하세요."
        ;;
    esac
  done
}
