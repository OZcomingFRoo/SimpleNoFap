# SimpleNoFap Agent Instructions

## Project Purpose

SimpleNoFap is a small Android app that helps users track a NoFap streak and stay encouraged through a live counter, Day-Streak rewards, scheduled notifications, and a future widget. Keep the app simple, personal, and focused on helping the user continue their streak.

Use `README.md` for the product overview and `dev_vision/` for early feature direction. Those files are rough planning notes, but they are still the current source for product intent.

## Current Stack

- Kotlin Android app with a single `:app` Gradle module.
- Jetpack Compose UI with Material3.
- DataStore Preferences for current settings.
- Existing settings access goes through `SettingsRepository`.
- Existing localization goes through `AppStrings`, `LocalAppStrings`, `ResolvedLanguage`, and `LocalLayoutDirection`.
- Existing theme entrypoint is `SimpleNoFapTheme`.

## Development Rules

- Preserve Hebrew and English support in every UI change.
- Preserve RTL/LTR behavior. Hebrew must render right-to-left through the existing layout direction flow.
- Do not hardcode user-facing text directly in composables when it should be localized. Add it to `AppStrings` and both language definitions.
- Preserve Material You dynamic color behavior. Prefer `MaterialTheme.colorScheme` for UI colors instead of app-wide hardcoded colors.
- Keep UI consistent with Material3 and the current Compose structure.
- New persistent data access must use the repository pattern. UI code should consume state exposed by repositories or view-model-facing abstractions, not read or write storage directly.
- Keep changes small and aligned with the existing package structure unless a task explicitly asks for a larger refactor.

## Architecture Direction

- DataStore Preferences is for lightweight user settings such as language, theme, name, and streak start time.
- A database is intended for structured app data such as scheduled notifications and Day-Streak rewards.
- Repositories are the boundary between storage and the rest of the app.
- UI should be mostly declarative Compose state rendering. Business rules and persistence logic should not live inside composables.

## Common Commands

Allowed routine commands:

- `git pull`
- `git fetch`
- `git commit` with a clear message
- `.\gradlew.bat test`
- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat compileDebugKotlin`
- Android Studio "Sync Project with Gradle Files" equivalent

If Gradle needs the Android Studio JBR on this machine, use:

```powershell
$env:JAVA_HOME='V:\Android\Android Studio\jbr'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

## GitHub Issue Workflow

- The AI may read, write, and update GitHub issues when the user asks.
- Capture issue context locally under `.ai-issues/issue-<number>/`.
- `.ai-issues/` is ignored by Git and is only a working area for AI sessions.
- When preparing a new issue, write it clearly enough for a later AI session to implement without rediscovering the whole discussion.
- Do not close GitHub issues unless the user explicitly asks.
- When closing an issue, leave a detailed Markdown comment that references the commit, branch, or pull request that completed the work.

## Before Finishing

- Check `git status --short`.
- Run the most relevant Gradle verification command when practical.
- Report any verification that could not be run.
