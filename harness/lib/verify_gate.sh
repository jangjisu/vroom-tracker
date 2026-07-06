#!/usr/bin/env sh

check_verify_gate() {
  state_file="$1"
  step="$2"
  hook_name="$3"

  verify_dir="$ROOT_DIR/harness/hooks/verify"

  for verify_hook in "$verify_dir"/*.sh; do
    [ -f "$verify_hook" ] || continue
    case "$(basename "$verify_hook")" in
      00-check-review-gate.sh) continue ;;
    esac

    set +e
    output=$("$verify_hook" "$state_file")
    code=$?
    set -e

    if [ "$code" -eq 0 ]; then
      continue
    fi

    message=$(printf '%s\n' "$output" | awk -F= '/^MESSAGE=/ { print substr($0, 9); exit }')
    [ -n "$message" ] || message="verify 단계 게이트가 완료되지 않았습니다."

    case "$code" in
      20)
        fail_needs_user "$step" "$hook_name" "$message" "verify" \
          "먼저 harness/harness.sh verify verify를 통과하세요."
        ;;
      30)
        fail_blocked "$step" "$hook_name" "$message" "verify" \
          "먼저 harness/harness.sh verify verify를 통과하세요."
        ;;
      *)
        fail_autofixable "$step" "$hook_name" "$message" "verify" \
          "먼저 harness/harness.sh verify verify를 통과하세요."
        ;;
    esac
  done
}
