# Six-Step Harness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expand the project harness to six gates and add concise product, data, architecture, and quality reference documents.

**Architecture:** Keep shell hooks as deterministic gates. Add a plan-review gate between spec analysis and implementation, make specialist reviews conditional, and keep implementation guidance in version-controlled reference documents.

**Tech Stack:** POSIX shell, ast-grep, Gradle, JaCoCo, Markdown

---

### Task 1: Add project reference documents

**Files:**
- Create: `PRODUCT.md`
- Create: `DATA.md`
- Create: `ARCHITECTURE.md`
- Create: `QUALITY.md`
- Track: `API.md`
- Modify: `.gitignore`

- [x] Record current state, agreed goals, and undecided items in each document.
- [x] Keep external API request and response details in `API.md`.

### Task 2: Expand the harness to six gates

**Files:**
- Modify: `AGENTS.md`
- Modify: `harness/README.md`
- Modify: `harness/harness.sh`
- Create: `harness/steps/03-plan-review.md`
- Move: existing step documents 3 through 5 to steps 4 through 6
- Create: `harness/hooks/review/*`
- Modify: `harness/runs/current/state.env.example`

- [x] Add the `review` command between `spec` and `code`.
- [x] Require an updated plan, recorded trade-off, review decision, and user approval.
- [x] Document conditional gstack review and the implementation handoff.

### Task 3: Add deterministic quality gates

**Files:**
- Create: `harness/hooks/code/09-check-no-else.sh`
- Modify: `harness/steps/04-code.md`
- Modify: `harness/steps/05-verify.md`
- Modify: `build.gradle`

- [x] Reject `else` and `else if` in changed Java files using ast-grep.
- [x] Keep the current total coverage floor and reject total coverage regression.
- [x] Enforce 100% changed executable-line coverage and changed branch coverage from JaCoCo XML evidence.

### Task 4: Verify the harness

**Files:**
- Test: shell scripts and Markdown references changed above

- [x] Run shell syntax checks for every harness script.
- [x] Run focused pass and failure scenarios for the new review and no-else hooks.
- [x] Run the project test and JaCoCo verification tasks.
- [x] Review the final diff and stage only this harness change.
