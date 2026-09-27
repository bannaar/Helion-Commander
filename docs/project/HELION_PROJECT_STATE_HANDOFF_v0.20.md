# HELION PROJECT / AGENT HANDOFF STATE v0.20

Date: 2026-09-26

## Runtime authority

HELION's authoritative product path is the native C++ persistent server plus
native SDL2/OpenGL client.

The browser prototype is legacy reference.

## Current engineering operation

Batch 14 remains the active unfinished engineering batch.

Do not infer completion from old prompts. Audit the repository and
`docs/development/BATCH14_EXECUTION.md`.

Known recovery branch/history from the handoff package:
- branch: `batch14/implementation-v0.11`
- `3764db1` WIP recovery checkpoint
- `b19f08a` WIP recovery checkpoint
- `5ed7772` bounded binary protocol foundation
- `80454d3` authoritative galaxy persistence/query
- `e9edfe0` galactic topology foundation

The actual current repository may be ahead.

## Current destination

Batch 14 must finish with:
- exact 200-system Known Space;
- 120 faction-controlled systems;
- 80 Zero Space systems;
- exact 27/25/25/23/20 faction allocation;
- meaningful Zero Space geography;
- coherent non-sovereign IGG representation;
- current protocol/persistence compatibility;
- current GSC/UniNet/canon docs;
- relevant tests/build/security/package validation;
- completion report;
- no Batch 15 work.

## Current canon

World Bible:
`docs/lore/HELION_World_Bible_draftcomp7.txt`

Canon sync:
`docs/lore/HELION_CANON_CHANGELOG_AI_SYNC_v0.20.txt`

Chronology:
- Severance 187 AS
- present 1087 AS
- 900-year interval

DraftComp7 now includes a living-universe layer of named leaders, historical
figures, military characters, pilots/crews, criminals, mysteries, cults,
guilds, fleets, corporate boards, businesses, brands, media, advertisers,
ordinary citizens, and social history.

That new lore is not automatic Batch 14 code scope.

## Agent control

Gemini:
- `GEMINI.md`
- `/goal`
- `/resume14`

Codex:
- `AGENTS.md`
- `HELION_Codex_Goal_v0.20.txt`
- `HELION_Batch14_Codex_Resume_Prompt_v0.20.txt`

Both agents must use Git/tests/repository evidence as implementation truth.