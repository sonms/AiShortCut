---
name: using-android-skills
description: Use when a task requires changing the Android build/toolchain surface — AGP/Gradle version upgrades, Gradle plugin or Kotlin/Compose compiler compatibility, AndroidManifest, R8/proguard, edge-to-edge or targetSdk bumps, Play policy or billing, CameraX, perfetto traces, intent security — BEFORE hand-rolling the approach or fetching migration guides. Do NOT trigger for ordinary app-code changes inside android/ (Compose UI edits, ViewModel/Retrofit changes, test code) that don't touch build config or these specific surfaces.
metadata:
  category: platform-dev
---

# Using Android Skills

## Overview

Google ships agent skills for Android tasks (the `android` CLI is installed).
For a covered task they beat hand-assembled research: authored by Google, dated, and bundled with steps.
The failure mode this skill exists for: an agent researches an AGP upgrade thoroughly and never learns the official skill for it is one command away.

The reverse failure mode also matters: don't let this skill fire for every file under `android/`. It's scoped to build/toolchain and the specific surfaces listed above — not general app-code work that happens to live in that directory.

## First move

```bash
android skills list
```

If a listed skill covers the task, install it into the project and **read and follow the installed SKILL.md** before planning further:

```bash
android skills add <skill-name> --project <repo-root>
```

The argument is positional — the docs' `--skill` flag does not exist in the CLI.
With a `.claude/` directory present the skill lands in `.claude/skills/` — Claude Code loads it as a project skill natively, no bridging needed.
The CLI also writes a second copy for other detected harnesses (observed: top-level `skills/` when `.claude/` exists, `.agents/skills/` when nothing is detected); keep it if other agent harnesses run in the repo, delete it otherwise.

**If the `android` CLI is not available:** say so explicitly, then fall back to official Android developer documentation for the specific task rather than repeating the failed command or guessing at flags.

## Task → skill mapping

| Task | Skill |
|---|---|
| AGP version bump, Gradle plugin compatibility, Kotlin/Compose compiler compatibility, build config changes | `agp-9-upgrade` |
| Release shrinker/keep rules, missing classes after minify | `r8-analyzer` |
| targetSdk 35+ insets (Compose-centric; for Flutter/RN only the embedding Activity part applies) | `edge-to-edge` |
| Exported components, deep links, OAuth redirect schemes | `android-intent-security` |
| Play Console policy questions | `play-policy-insights` |
| Billing Library version upgrade | `play-billing-library-version-upgrade` |
| Camera capture/preview work | `camerax` |
| Trace capture/analysis | `perfetto-trace-analysis` |

A skill that only tangentially matches is not worth an install; proceed without it rather than stretching the mapping.

## Gradle troubleshooting

Read this section only when you actually hit a Gradle cache or daemon error — not as general background.

**Check `GRADLE_USER_HOME` before searching a cache path.** It is not guaranteed to be `~/.gradle`. Run `echo $GRADLE_USER_HOME` first. Never conclude "not cached" from a search that only looked under `~/.gradle`.

**A Gradle cache error is usually a stale daemon, not corruption.** Symptoms: `Could not read workspace metadata from …/metadata.bin`, a null `FileLock` during plugin resolution, or a different missing-cache error on every build. These point at a daemon started before the cache changed.

Fix, in order:
1. `cd android && ./gradlew --stop` — stops only that wrapper's own daemon version.
2. If the error persists, list remaining Gradle daemon processes (e.g. `jps -l | grep GradleDaemon` or `ps aux | grep GradleDaemon`) and confirm which project/version each belongs to before terminating anything broader than step 1. Killing by pattern match (`pkill -f GradleDaemon`) can stop unrelated projects' builds or an IDE's daemon — only do this after confirming the process list, and get explicit user confirmation if other projects could be affected.
3. Remember an IDE can run its own Gradle daemon against the same `GRADLE_USER_HOME` (e.g. an editor's Gradle extension). `--stop` from the CLI does not reach it; that daemon needs to be disabled or given its own `GRADLE_USER_HOME` separately.

Never wipe `~/.gradle/caches` as a first response.

## Field notes

Space for this operator's own accumulated, project-specific findings — kept separate from the generic rules above so the defaults never go stale or leak into other projects/machines. Add entries only in this format, newest first:

```
- YYYY-MM-DD (project-name): what happened, what the fix was.
```

(empty — nothing logged yet)

## Boundaries

- **Per-project only, and only the 1–3 skills the task needs.** Never `--all`, never user scope — installing everything bloats every session's skill listing, and ecosystem-specific tooling stays project-local.
- The install adds tracked files to the repo (vendored, Google-authored SKILL.md files — see "Vendored files" below). Report which files were added. **Never commit them without asking first** — always surface the question to the user before running `git commit`, regardless of what the repo's apparent convention looks like.

### Vendored files

"Vendored" means an external party's source file was copied directly into this repo (not referenced via a package manager) — here, the SKILL.md content Google publishes for `android skills add`. Whether to commit vendored files, how to attribute them, and how to handle re-running `add` (it overwrites) are repo-policy questions, which is exactly why this skill doesn't decide it — it asks.
