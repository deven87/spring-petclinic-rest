# Approval Gates Reference

Each step ends with a gate: the user must approve before the orchestrator moves on. This catches bad requirements/design/code while it's still cheap to fix, instead of downstream.

Every gate offers the same three choices:
- **[A] Approve** — proceed to the next step
- **[B] Regenerate** — tell the agent what to change, it redoes the artifact, ask again
- **[C] Edit manually** — user edits the file directly, then the workflow re-validates it

| Gate | After Step | Question focus |
|------|-----------|-----------------|
| 1 | Requirements | Complete? Testable? No ambiguity? |
| 2 | Architecture | Meets requirements? Tech choices justified? |
| 3 | Design Review | Risks acceptable? Recommendations clear? |
| 4 | Implementation Plan | Tasks/dependencies/estimates reasonable? |
| 5 | Implementation | Build green, all tasks done? |
| 6 | Code Review | Critical/medium issues fixed? |
| 7 | Verification | Tests/coverage/security/perf all pass? |
| 8 | PR Creation | PR complete and ready for team review? |

See each step's own `.github/agents/*.agent.md` for its exact gate question and "Done when" checklist.
