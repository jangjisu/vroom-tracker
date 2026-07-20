#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
STATE_FILE="${HARNESS_STATE_FILE:-$ROOT_DIR/harness/runs/current/state.env}"

usage() {
  cat <<'USAGE'
Usage:
  harness/harness.sh list
  harness/harness.sh verify <plan|spec|review|code|verify|commit|all>

Steps:
  plan     - planning gate
  spec     - API/spec impact gate
  review   - finalized plan and user approval gate
  code     - code and test writing gate
  verify   - verification evidence gate
  commit   - git commit readiness gate
USAGE
}

step_dir() {
  case "$1" in
    plan) echo "$ROOT_DIR/harness/hooks/plan" ;;
    spec) echo "$ROOT_DIR/harness/hooks/spec" ;;
    review) echo "$ROOT_DIR/harness/hooks/review" ;;
    code) echo "$ROOT_DIR/harness/hooks/code" ;;
    verify) echo "$ROOT_DIR/harness/hooks/verify" ;;
    commit) echo "$ROOT_DIR/harness/hooks/commit" ;;
    *) return 1 ;;
  esac
}

run_step() {
  step="$1"
  dir=$(step_dir "$step") || {
    echo "Unknown step: $step" >&2
    usage >&2
    exit 2
  }

  echo "==> step: $step"

  for hook in "$dir"/*.sh; do
    [ -f "$hook" ] || continue
    echo "--> hook: $(basename "$hook")"

    set +e
    output=$("$hook" "$STATE_FILE")
    code=$?
    set -e

    printf '%s\n' "$output"

    case "$code" in
      0) ;;
      10)
        echo "RESULT=STEP_FAILED_AUTOFIXABLE"
        exit 10
        ;;
      20)
        echo "RESULT=STEP_FAILED_NEEDS_USER"
        exit 20
        ;;
      30)
        echo "RESULT=STEP_BLOCKED"
        exit 30
        ;;
      *)
        echo "RESULT=STEP_FAILED_UNKNOWN"
        exit 99
        ;;
    esac
  done

  echo "RESULT=STEP_PASS"
}

if [ "$#" -lt 1 ]; then
  usage >&2
  exit 2
fi

command="$1"
shift

case "$command" in
  list)
    usage
    ;;
  verify)
    if [ "$#" -ne 1 ]; then
      usage >&2
      exit 2
    fi

    if [ "$1" = "all" ]; then
      for step in plan spec review code verify commit; do
        run_step "$step"
      done
    else
      run_step "$1"
    fi
    ;;
  *)
    usage >&2
    exit 2
    ;;
esac
