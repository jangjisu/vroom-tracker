#!/usr/bin/env sh

hook_result() {
  status="$1"
  step="$2"
  hook="$3"
  message="$4"
  regress_to="${5:-$step}"
  action="${6:-}"

  printf 'STATUS=%s\n' "$status"
  printf 'STEP=%s\n' "$step"
  printf 'HOOK=%s\n' "$hook"
  printf 'MESSAGE=%s\n' "$message"
  printf 'REGRESS_TO=%s\n' "$regress_to"
  if [ -n "$action" ]; then
    printf 'SUGGESTED_ACTION=%s\n' "$action"
  fi
}

pass() {
  hook_result "PASS" "$1" "$2" "$3" "${4:-$1}" "${5:-}"
  exit 0
}

fail_autofixable() {
  hook_result "FAIL_AUTOFIXABLE" "$1" "$2" "$3" "${4:-$1}" "$5"
  exit 10
}

fail_needs_user() {
  hook_result "FAIL_NEEDS_USER" "$1" "$2" "$3" "${4:-$1}" "$5"
  exit 20
}

fail_blocked() {
  hook_result "FAIL_BLOCKED" "$1" "$2" "$3" "${4:-$1}" "$5"
  exit 30
}
