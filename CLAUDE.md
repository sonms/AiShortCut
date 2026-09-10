# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Product concept

An AI-trend curation app for both iOS and Android, built to counter the volatility/disposability of SNS feeds by giving AI news and model releases a persistent, revisitable home. KMP/CMP is the deliberate choice here specifically to reach both mobile ecosystems from one codebase.

Three core screens (Material 3 bottom nav):

- **Home** — daily AI trend feed: trending keyword chips, swipeable daily-summary cards, Hugging Face daily top models list.
- **Discover** — benchmark graphs (MMLU, HumanEval, etc.) and live search/filter over topics (RAG, LoRA, ...).
- **Saved** — offline-persisted bookmarks (articles, favorited models), organized by tag.

UI design system (color palette → Material 3 `ColorScheme` mapping, typography, spacing, component rules): see `DESIGN.md`. Follow it when building any `presentation:*` UI.

Data sources (build in this order — Hugging Face first, it needs no auth):

1. Hugging Face Trending API
2. GitHub API (trending AI repos)
3. AI news RSS/Atom feeds (Arxiv etc.)

Open decisions, don't assume an answer:

- **Local persistence**: moving to Room (KMP) — `data:home`'s current `HomeLocalStore` (hand-rolled `expect`/`actual`) predates this decision and is the pattern to migrate away from, not to copy into new `data:*` modules.
- **Benchmark chart rendering**: undecided until real benchmark data is flowing (Canvas API vs. a KMP chart library like KoalaPlot). Don't pick one without asking.

## Module architecture

KMP project, Android + iOS targets. Every module under `core:*`, `data:*`, `presentation:*` applies one of the precompiled convention plugins from `build-logic/convention` — never apply `com.android.library` or `com.android.application` directly to a feature module, that produces a plain AGP library (no iOS target, wrong source-set layout, needs an `AndroidManifest.xml` it shouldn't).

| Layer | Plugin id | Adds |
|---|---|---|
| `core:*` | `aishortcut.kmp.library` | Android+iOS targets, compileSdk/minSdk, kotlin-test |
| `data:*` | `aishortcut.kmp.library.data` | + Ktor, kotlinx.serialization (okhttp engine on Android, darwin on iOS) |
| `presentation:*` | `aishortcut.kmp.library.compose` | + Compose Multiplatform, lifecycle-compose |

New module checklist: add `include(":layer:name")` to `settings.gradle.kts` → `build.gradle.kts` with the plugin id above + `namespace` → source under `src/commonMain/kotlin`, add `src/androidMain`/`src/iosMain` only when an `expect`/`actual` split is actually needed.

**Entry points live in the platform app shells, not in `presentation:*`.** `presentation:*` modules are KMP libraries — no `AndroidManifest.xml`, no way to register an `Activity`. `MainActivity` (`androidApp`) and `MainViewController` (`shared/iosMain`) are the only things that construct the Compose UI tree (`presentation:main`'s `MainApp()`); they contain no screen logic themselves.

## Data layer conventions

Inside a `data:*` module, transport DTOs and domain models stay in separate files:

- `*Dto.kt` holds the `@Serializable` wire types **and** the `toDomain()` mapper. The dependency runs one way only — DTO knows about the domain type, never the reverse.
- Each domain type gets its own file named after the type, with no serialization annotations, no `kotlinx.serialization` / Ktor imports — just a plain `data class`.
- Repositories return domain types only. Nothing outside the `data:*` module ever names a `*Dto` type.
- Pattern to copy: `data/hfTrending`'s `HfTrendingDto.kt` + `TrendingModel.kt`.

## Build

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `/iosApp` in Xcode and run from there
- Single module: `./gradlew :layer:name:build`

## Commits

Conventional Commits, scoped to the module path: `feat(data:home): add HomeRepository backed by local storage`.

## Verification

- **Done means**: `./gradlew :layer:name:build` passes (or `:androidApp:assembleDebug` for app-level changes), and any non-trivial new logic has at least one `commonTest` test (pattern: `data/hfTrending`'s `HfTrendingDtoTest.kt`).
- **iOS**: cannot be built or run from this dev machine (Windows). Verify iOS-facing code by keeping the logic in `commonMain` and confirming it compiles; the actual iOS build/run is the user's job in Xcode. Don't claim iOS is verified.
- **Retry policy**: on a build/test failure, read the log and fix the cause. If two or three attempts don't resolve it, stop and report the failure with its output rather than guessing further.
- Judge results by Gradle build and test output, not by confidence.

## When to add an agent graph (not now)

Single developer, sequential work — one agent plus the checklists above is enough. Revisit a multi-agent / multi-stage split only when one of these is actually true:

- A stage genuinely needs a different access scope (e.g. a step that must not touch secrets or credentials).
- The independent data sources (Hugging Face / GitHub / RSS) are being built in parallel by separate agents and context bleed or merge conflicts become real.
- One change spans expertise that doesn't fit a single context (e.g. Gradle convention-plugin surgery + Compose UI + iOS interop in one pass).
