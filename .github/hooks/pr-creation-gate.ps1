# PreToolUse hook: blocks PR-creation tool calls (gh CLI or GitHub MCP) until the final gate passes.
# Checks: feature branch, clean working tree, all 6 SDLC artifacts present, tests passing.
#
# Hook contract (VS Code Copilot Local harness): the harness pipes the tool-call JSON to stdin.
#   exit 0 -> allow, nothing shown
#   exit 2 -> stderr is shown to the model and the tool call is denied
#
# Can also be run directly for a self-check: powershell -File pr-creation-gate.ps1 -Check

param(
    [switch]$Check
)

$ErrorActionPreference = "Continue"
$ROOT = (Resolve-Path "$PSScriptRoot/../..").Path

function Test-Ready {
    Push-Location $ROOT
    $fail = 0
    
    # Check branch name
    $branch = & git rev-parse --abbrev-ref HEAD 2>$null
    if ($branch -eq "main" -or $branch -eq "master") {
        Write-Error "On '$branch' — create a feature branch before opening a PR" -ErrorAction Continue
        $fail = 1
    }
    
    # Check for uncommitted changes (ignore pr-description.md which may be in draft)
    $uncommitted = & git status --porcelain 2>$null | Where-Object { $_ -notmatch "pr-description.md" }
    if ($uncommitted) {
        Write-Error "Uncommitted changes — commit or stash before creating a PR" -ErrorAction Continue
        $fail = 1
    }
    
    # Check for all 6 SDLC artifacts
    $artifacts = @("requirements.md", "architecture.md", "design-review.md", "impl-plan.md", "code-review.md", "verification-report.md")
    foreach ($artifact in $artifacts) {
        $path = Join-Path $ROOT $artifact
        if (-not (Test-Path $path)) {
            Write-Error "$artifact missing" -ErrorAction Continue
            $fail = 1
        }
    }
    
    # Run tests
    if (Test-Path "$ROOT/pom.xml") {
        # Maven project
        $mvn = if (Test-Path "$ROOT/mvnw.cmd") { "$ROOT/mvnw.cmd" } else { "mvn" }
        & $mvn -B -q clean test 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Tests are failing" -ErrorAction Continue
            $fail = 1
        }
    }
    elseif (Test-Path "$ROOT/package.json") {
        # Node.js project
        & npm test --silent 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Tests are failing" -ErrorAction Continue
            $fail = 1
        }
    }
    
    Pop-Location
    return $fail
}

# If run with --Check flag, just do the check and exit
if ($Check) {
    if ((Test-Ready) -eq 0) {
        Write-Host "Ready to create the PR"
        exit 0
    }
    exit 1
}

# Otherwise, read stdin and check if this is a PR-creation tool call
$input_text = $input -join "`n"
if ($input_text -notmatch "(gh pr create|create_pull_request|create-pull-request)") {
    # Not a PR creation tool, allow it
    exit 0
}

# This IS a PR creation tool, run the gate check
if ((Test-Ready) -ne 0) {
    Write-Error "PR creation blocked by the pre-PR gate — fix the issues above." -ErrorAction Continue
    exit 2
}

exit 0
