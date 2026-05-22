#!/usr/bin/env sh

load_state() {
  state_file="$1"

  if [ ! -f "$state_file" ]; then
    echo "Missing state file: $state_file" >&2
    echo "Create it from harness/runs/current/state.env.example" >&2
    exit 10
  fi

  # shellcheck disable=SC1090
  . "$state_file"
}

repo_root() {
  git rev-parse --show-toplevel 2>/dev/null || pwd
}

changed_files() {
  git diff --name-only --diff-filter=ACMRTUXB HEAD 2>/dev/null
  git ls-files --others --exclude-standard 2>/dev/null
}

changed_java_files() {
  changed_files | awk '/\.java$/ { print }' | sort -u
}

changed_production_java_files() {
  changed_java_files | awk '/^src\/main\/java\// { print }'
}
