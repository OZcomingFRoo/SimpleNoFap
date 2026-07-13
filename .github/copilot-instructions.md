# Copilot Instructions

Use `AGENTS.md` as the canonical project guidance. This file is only a short compatibility summary for GitHub Copilot.

Core rules:

- SimpleNoFap is a small Kotlin Android app using Jetpack Compose and Material3.
- Preserve Hebrew and English support in every UI change.
- Preserve RTL/LTR behavior through the existing localization and layout direction flow.
- Do not hardcode user-facing text in composables when it should be localized.
- Preserve Material You dynamic color behavior through `SimpleNoFapTheme` and `MaterialTheme.colorScheme`.
- Use the repository pattern for new persistent data access.
- Keep business rules and persistence logic out of composables.
- Keep changes small and aligned with the existing package structure.
