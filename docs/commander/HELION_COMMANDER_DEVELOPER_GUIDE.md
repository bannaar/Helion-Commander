# HELION Commander Developer Guide

**Project:** HELION Commander  
**Platform:** Android  
**Language:** Kotlin  
**UI:** Jetpack Compose  
**Status:** Pre-server-integration alpha  
**Current backend routing:** `DEMO -> FakeCompanionApi`; `PRIVATE_TEST/PRODUCTION -> RealCompanionApi (NOT CONFIGURED)`
**Architecture:** HELION server-authoritative companion client

---

## 1. Purpose

HELION Commander is the Android companion application for HELION.

It is intended to provide away-from-game access to commander information, fleet planning, navigation, markets, UniNet, organizations, communications, missions, and other companion-safe services.

HELION Commander is **not** a second HELION game server.

The core rule is:

```text
COMMANDER REQUESTS
        ↓
HELION SERVER VALIDATES
        ↓
HELION SERVER COMMITS
        ↓
COMMANDER RECEIVES AUTHORITATIVE RESULT
```

The Android client must never independently create authoritative truth for:

- GSC
- ship ownership
- fitted modules
- cargo or inventory
- mission completion
- market prices or stock
- standings
- organizations
- player location
- sovereignty
- discovery state
- progression
- other persistent gameplay state

Local planning data is allowed, including fitting plans, route plans, bookmarks, market watchlists, UI preferences, and cached already-authorized information.

---

## 2. Current Status

The current application is a validated Android prototype.

Current data flow:

```text
HELION Commander
       ↓
Environment selection
       ↓
CompanionApiFactory / DelegatingCompanionApi
       ├── DEMO          -> FakeCompanionApi
       ├── PRIVATE TEST  -> RealCompanionApi [NOT CONFIGURED]
       └── PRODUCTION    -> RealCompanionApi [NOT CONFIGURED]
```

Each environment has a separate Room database and credential namespace. Repository DAO access and long-lived Room observers must rebind when the selected environment changes, and environment-scoped in-memory state must be cleared before target-environment data is loaded.

There is currently **no verified `RealCompanionApi` connection to a HELION PRIVATE TEST or PRODUCTION server**.

Do not present mock data as live server data.

Current validation commands:

```bash
gradle testDebugUnitTest
gradle assembleDebug
git diff --check
```

Do not call a new checkpoint validated until its exact head commit has passed the unit-test and debug-build CI gates.

---

## 3. Read Before Development

Read these in order before substantial changes:

```text
AI_STUDIO_INSTRUCTIONS.md
AI_STUDIO_BOOTSTRAP_PROMPT.txt

docs/commander/HELION_COMMANDER_PRODUCT_SPEC.md
docs/commander/HELION_COMMANDER_CONTEXT.md
docs/commander/HELION_COMMANDER_SERVER_INTEGRATION.md
docs/commander/HELION_COMMANDER_CANON_MIGRATION_CHECKLIST.md
docs/commander/HELION_COMMANDER_API_RECONCILIATION.md

docs/project/HELION_ACTIVE_GOAL_v0.20.md
docs/project/HELION_PROJECT_STATE_HANDOFF_v0.20.md

docs/canon/HELION_World_Bible_draftcomp7.txt
docs/canon/HELION_CANON_CHANGELOG_AI_SYNC_v0.20.txt
docs/canon/HELION_CANON_AND_DOC_RECONCILIATION_v0.20.md

docs/COMPANION_API_CONTRACT.md
```

The proposed API contract is a design document. It is **not proof that the endpoints, transport, auth flow, or event system already exist**.

Verify all real server behavior against the HELION server repository and runtime.

---

## 4. Authority Order

When information conflicts, use this order:

1. Verified HELION server/runtime behavior
2. Current HELION Commander source and tests
3. Commander product specification
4. Commander context
5. Commander server integration document
6. HELION Active Goal
7. HELION Project State
8. Current World Bible
9. Canon changelog/reconciliation
10. Historical documents

---

## 5. Project Structure

Main organization:

```text
app/src/main/java/com/example/helion/

core/
├── HelionAppContainer.kt
├── model/
├── network/
│   ├── CompanionApi.kt
│   └── FakeCompanionApi.kt
├── database/
├── navigation/
└── repository/

ui/
├── HelionApp.kt
├── components/
├── home/
├── commander/
├── fleet/
├── universe/
├── market/
├── galnet/
├── guild/
├── comms/
├── missions/
└── settings/
```

Some internal names such as `galnet`, `credits`, `NULL_SECURITY`, and `AVOID_NULLSEC` are compatibility-sensitive legacy identifiers. They do not define current player-facing canon.

---

## 6. Current Player-Facing Canon

Use:

- UniNet
- Galactic Standard Credit / GSC
- Zero Space
- IGG
- INSA
- Gatewatch
- Galactic Defense Force / GDF
- Crownspire
- Ironstar Forge
- Verdance
- Riftspace
- Adaptive Systems Architecture / ASA

Do not restore obsolete current-facing terminology such as:

- GalNet
- generic Credits / CR
- Null-Sec
- Helion Concordat as the current umbrella authority
- Concordia as the Commonwealth capital
- Titan Forge
- New Eden

Compatibility-sensitive internal identifiers may remain until a deliberate tested migration exists.

---

## 7. Universe Rules

Known Space contains exactly 200 persistent systems:

```text
Helion Commonwealth       27
Solar Directorate         25
Free Systems Compact      25
Meridian League           23
Aurelian Synod            20
--------------------------------
Faction-controlled       120

Zero Space                80
--------------------------------
Known Space total        200
```

Riftspace is outside that count.

Keep these concepts distinct:

- space domain
- security classification
- sovereignty
- structure ownership
- discovery knowledge

Zero Space is not The Null.

Aurelia is an Aurelian Synod center, not generic player-owned 0.0 territory.

---

## 8. Server Authority and API Boundaries

Repositories should depend on `CompanionApi`, not environment-specific implementations.

Target architecture:

```text
                        CompanionApi
                             │
             ┌───────────────┴───────────────┐
             │                               │
      FakeCompanionApi                RealCompanionApi
             │                               │
      DEMO / OFFLINE             PRIVATE TEST / PRODUCTION
```

`FakeCompanionApi` is development/demo infrastructure.

It must never imply a successful connection to TEST or PRODUCTION.

`RealCompanionApi` must **not** implement `DevelopmentSimulationApi`.

Developer simulation controls belong only to local/mock or explicitly authorized private-test infrastructure.

---

## 9. Development Setup

Clone:

```bash
git clone https://github.com/bannaar/Helion-Commander.git
cd Helion-Commander
```

Check Java:

```bash
java -version
```

Build/test:

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

Inspect state:

```bash
git status
git branch --show-current
git log --oneline -10
```

---

## 10. Branch Workflow

Do not develop substantial work directly on `main`.

```bash
git checkout main
git pull
git checkout -b feature/your-feature-name
```

Examples:

```text
feature/developer-server-select
feature/real-server-status
feature/commander-auth
feature/live-fleet-read-model
```

Before committing:

```bash
gradle testDebugUnitTest
gradle assembleDebug
git diff --check
git status
git diff
```

Then commit and push through a pull request.

---

## 11. Current Feature Areas

### Home
Commander, active ship, route, watched market, UniNet, and service summaries using simulated data.

### Commander
Identity, callsign, GSC, rank, XP, career, standings, permits, and endorsements.

### Fleet
Owned-ship presentation, loadout planning, hardpoints, utilities, core/optional modules, mass/power calculations, and livery previews.

A local fitting plan is not an authoritative fitted ship.

### Universe
2D map, system details, starlanes, route calculation, route bookmarks, and risk-oriented route policies.

### Market
Mock prices, stock, demand, comparison, margins, cargo-profit calculations, watchlists, and alerts.

The real client must never directly set authoritative commodity prices.

### UniNet
News categories plus local read/bookmark state. Legacy package names may still contain `galnet`.

### Guild / Alliance
Profile, roster, notices, permissions, and territory presentation.

Guild membership does not imply access to all organization intelligence.

### Comms
Prototype conversations and messaging.

Do not claim cryptographic properties that the implemented transport does not provide.

### Missions
Prototype tactical missions and statistics using mock data.

---

## 12. Local Persistence

Room/local storage is suitable for:

- cached authorized information
- route bookmarks
- fitting plans
- market alerts
- UI preferences
- local planning state

DEMO, PRIVATE TEST, and PRODUCTION data are required to remain isolated at runtime.

Target:

```text
PRIVATE TEST
├── credentials/session
├── cache
└── database namespace

PRODUCTION
├── credentials/session
├── cache
└── database namespace
```

Never silently copy or fall back between environments.

---

## 13. Current Milestone: Multi-Environment Server Foundation (M1)

M1 is implemented in the current codebase and must remain green under its isolation acceptance tests.

Current modes:

```text
DEMO / OFFLINE
PRIVATE TEST
PRODUCTION
```

Implemented foundation:

- `ServerEnvironment` and `ServerProfile`
- persistent environment selection
- server selector UI with unmistakable environment identity
- Production switch confirmation
- environment-specific credential namespaces
- separate Room databases per environment
- runtime DAO rebinding when environments change
- cancellation/rebinding of long-lived Room observers
- clearing of environment-scoped in-memory state during a switch
- `CompanionApiFactory` and `DelegatingCompanionApi`
- `RealCompanionApi` skeleton
- explicit server-not-configured state
- server-status abstraction
- cross-environment isolation tests
- DEMO-only simulation powers that do not leak into TEST/PRODUCTION

Until a verified real endpoint exists:

```text
DEMO          -> FakeCompanionApi
PRIVATE TEST  -> RealCompanionApi [NOT CONFIGURED]
PRODUCTION    -> RealCompanionApi [NOT CONFIGURED]
```

Selecting TEST or PRODUCTION changes the target environment and isolated local namespace. It must not imply that a live server connection exists.

---

## 14. First Real Server Integration

After server selection exists, integrate in this order:

```text
SERVER STATUS
      ↓
AUTHENTICATION
      ↓
COMMANDER SUMMARY
      ↓
OWNED FLEET
      ↓
PERMISSION-FILTERED UNIVERSE
      ↓
UNINET
      ↓
MARKETS
      ↓
MISSIONS
      ↓
SAFE SERVER-VALIDATED MUTATIONS
```

Do not begin with live markets, remote fitting, or other high-impact mutations.

---

## 15. Authentication Rules

Future authorization should be:

- scoped
- revocable
- expiring
- environment-bound

Never commit:

- passwords
- API secrets
- private signing keys
- production tokens
- developer/admin credentials

TEST credentials must not automatically become PRODUCTION credentials.

---

## 16. Private TEST Environment

PRIVATE TEST may eventually support:

- ordinary player
- invited tester
- developer/admin

Invited testers do not automatically receive developer powers.

Future TEST-only server commands may include:

- grant test GSC
- grant ships/modules
- set XP/rank
- modify test inventory
- set standings
- move a test character
- reset test state

Commander must not perform these locally. The TEST server must authorize and execute them.

---

## 17. Production Environment

PRODUCTION represents the persistent live universe.

Production must not expose TEST developer controls.

Server authorization, not UI visibility, is the security boundary.

The UI should make live state unmistakable:

```text
LIVE // PERSISTENT UNIVERSE
```

Switching into Production should require explicit confirmation.

---

## 18. Information Security

Never send hidden truth to the client and rely on UI hiding it.

The server should return purpose-specific, permission-filtered read models.

Protected information may include:

- undiscovered anomalies
- unearned survey results
- private organization intelligence
- admin/moderation information
- credentials
- database-only fields
- unrevealed narrative/mystery information

Where implemented, discovery can progress:

```text
DETECTED -> CLASSIFIED -> LOCATED -> SURVEYED
```

Commander receives only authorized knowledge.

---

## 19. Testing Rules

Never weaken a valid test merely to get green CI.

Server-environment work should prove:

- DEMO uses mock data
- TEST/PRODUCTION do not silently use `FakeCompanionApi`
- environment selection persists
- Production switching requires confirmation
- TEST and Production credentials are isolated
- TEST and Production caches do not cross
- repository reads/writes follow the newly selected environment after a runtime switch
- long-lived Room observers rebind to the newly selected environment
- DEMO authoritative-looking in-memory state is cleared before TEST/PRODUCTION refreshes
- `RealCompanionApi` cannot expose development simulation powers
- DEMO simulation controls remain available only while DEMO is the active delegated API

Final validation:

```bash
gradle testDebugUnitTest
gradle assembleDebug
git diff --check
```

---

## 20. Definition of Done

A Commander feature is complete only when:

- server authority remains intact
- current canon terminology is preserved
- mock/live state is clearly distinguished
- TEST/Production boundaries are respected
- relevant tests exist
- existing tests remain green
- debug APK builds
- docs match actual implementation
- planned features are not described as implemented

When uncertain, prefer:

```text
UNKNOWN / NOT IMPLEMENTED
```

over inventing server behavior.

HELION Commander should be a trustworthy window into HELION, not a parallel copy of the universe.
