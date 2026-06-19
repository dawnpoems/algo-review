# AGENTS.md

## Branch strategy

This repository uses a simplified Git Flow.

- `main`: production branch. Code merged here is considered releasable and may be deployed to the live server.
- `dev`: integration branch for the next release.
- `feat/*`: feature branches. Must open pull requests into `dev`.
- `fix/*`: development bug fix branches. Must open pull requests into `dev`.
- `chore/*`: maintenance branches. Must open pull requests into `dev`.
- `docs/*`: documentation branches. Must open pull requests into `dev`.
- `refactor/*`: refactoring branches. Must open pull requests into `dev`.
- `hotfix/*`: urgent production fix branches. Must branch from `main` and open pull requests into `main`.

## Pull request rules

- Never push directly to `main`.
- Never push directly to `dev`.
- For normal implementation tasks, start from `dev`.
- For normal implementation tasks, open a draft PR against `dev`.
- Only release PRs from `dev` or urgent `hotfix/*` PRs may target `main`.
- After a `hotfix/*` PR is merged into `main`, ensure the same fix is reflected in `dev`.

## Codex working rules

- Unless explicitly instructed otherwise, assume the target branch is `dev`.
- Create small, reviewable pull requests.
- Do not change unrelated files.
- Do not change API contracts unless the task explicitly requires it.
- Run available tests/builds before completing a task.
- Summarize changed files, commands run, test results, assumptions, and remaining risks.
- Open a draft PR when the task is complete.
