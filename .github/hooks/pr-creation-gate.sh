#!/bin/bash
# ⚠️ DEPRECATED: This script has been replaced by pr-creation-gate.ps1 (PowerShell).
# Use pr-creation-gate.ps1 instead for Windows/cross-platform support.
# This file is kept for reference only.
#
# PreToolUse hook: blocks PR-creation tool calls (gh CLI or GitHub MCP) until the final gate
# passes: feature branch, clean working tree, all 6 SDLC artifacts present, tests passing.
#
# Hook contract (VS Code Copilot Local harness): the harness pipes the tool-call JSON to stdin.
#   exit 0 -> allow, nothing shown
#   exit 2 -> stderr is shown to the model and the tool call is denied
#
# Can also be run directly for a self-check: bash pr-creation-gate.sh --check

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

check_ready() {
    cd "$ROOT" || return 1
    local fail=0 branch
    branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null)"
    if [[ "$branch" == "main" || "$branch" == "master" ]]; then
        echo "On '$branch' — create a feature branch before opening a PR" >&2
        fail=1
    fi
    # pr-description.md may still be uncommitted while the PR text is being drafted
    if [[ -n "$(git status --porcelain | grep -v 'pr-description.md')" ]]; then
        echo "Uncommitted changes — commit or stash before creating a PR" >&2
        fail=1
    fi
    for artifact in requirements.md architecture.md design-review.md impl-plan.md code-review.md verification-report.md; do
        [[ -f "$ROOT/$artifact" ]] || { echo "$artifact missing" >&2; fail=1; }
    done
    if [[ -f pom.xml ]]; then
        local mvn="./mvnw"; [[ -x "$mvn" ]] || mvn="mvn"
        "$mvn" -B -q clean test || { echo "Tests are failing" >&2; fail=1; }
    elif [[ -f package.json ]]; then
        npm test --silent || { echo "Tests are failing" >&2; fail=1; }
    fi
    return "$fail"
}

if [[ "$1" == "--check" ]]; then
    check_ready && echo "Ready to create the PR" && exit 0
    exit 1
fi

INPUT="$(cat)"
echo "$INPUT" | grep -qiE 'gh pr create|create_pull_request|create-pull-request' || exit 0

if ! check_ready; then
    echo "PR creation blocked by the pre-PR gate — fix the issues above." >&2
    exit 2
fi
exit 0
