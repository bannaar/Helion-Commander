# HELION ACTIVE DEVELOPMENT GOAL v0.20

Status: Active long-term engineering contract for Gemini CLI and Codex CLI
Date: 2026-09-26
Project: HELION

## 1. PRIMARY GOAL

Develop HELION as one coherent native, server-authoritative, persistent online
science-fiction universe.

Continue from the existing HELION repository. Do not restart, fork, or replace
working architecture merely because another agent would have designed it
differently.

The actual repository, tests, migrations, persistence behavior, and verified
execution records are authoritative for what is already implemented.

The current World Bible and this goal are authoritative for current canon and
long-term design direction.

## 2. CURRENT AUTHORITY CHAIN

Use these sources in this order for their respective domains:

IMPLEMENTATION STATE
1. Actual Git repository and working tree.
2. Current tests/builds/runtime behavior.
3. `docs/development/BATCH14_EXECUTION.md`.

LONG-TERM ENGINEERING GOAL
4. `docs/development/HELION_ACTIVE_GOAL_v0.20.md`.

CURRENT BATCH 14 CONTINUATION
5. Current agent-specific Batch 14 resume/handoff prompt.

CURRENT LORE / WORLD CANON
6. `docs/lore/HELION_World_Bible_draftcomp7.txt`.
7. `docs/lore/HELION_CANON_CHANGELOG_AI_SYNC_v0.20.txt`.

Historical prompts, old World Bibles, older goals, and recovery archives are
evidence and history. They do not override the current canon or current goal
when they conflict.

## 3. CORE ENGINEERING CONTRACT

- Preserve the native C++ persistent server and SDL2/OpenGL native client as
  the authoritative gameplay path.
- The browser prototype is legacy reference material, not a peer production
  universe.
- Preserve server authority.
- Clients request actions; the server validates and applies authoritative state.
- Preserve accounts, saves, ships, inventory, economy, reputation, missions,
  security, world state, and protocol compatibility unless an explicit tested
  migration is required.
- Reuse existing systems and service boundaries.
- Do not create parallel galaxy, economy, inventory, account, ship, mission,
  or companion backends.
- Keep DEVELOPMENT, PRIVATE TEST, and PRODUCTION state isolated.
- Keep secrets and production credentials out of the repository.
- Use deterministic, bounded, versioned protocol behavior.
- Add focused tests for every meaningful change.
- Repair code rather than weakening valid tests.
- Do not claim deferred systems are implemented.
- Do not start Batch 15 until Batch 14 is complete and the user explicitly
  authorizes it.
- Do not push, merge, squash, amend, reset, rebase, or rewrite recovery history
  without explicit user authorization.

## 4. CURRENT CANON CHRONOLOGY

The Severance occurred in **187 AS**.

The current/game-start year is **1087 AS**.

HELION begins exactly **900 years after the Severance**.

The old 287 AS present date and the old "roughly a century later" framing are
obsolete current canon.

Consequences:

- the five major powers are mature civilizations with centuries of history;
- IGG has centuries of treaty law, precedent, reform, missions, scandals,
  heroes, failures, and institutional culture;
- corporations, guilds, professions, brands, media, fleets, and spacer
  traditions may have long histories;
- modern ship standards, GSC, UniNet, regional markets, legal systems, and
  professional institutions evolved over centuries;
- no ordinary current-era person personally remembers the Severance;
- the passage of time does NOT imply that humanity solved the ancient Gates,
  the Architects, The Null, the Choir, all lost colonies, the true cause of
  the Severance, or all Riftspace phenomena.

## 5. CURRENT CANON TERMINOLOGY

Use current HELION-native terms in maintained canon and player-facing text:

- Inter-Galactic Governance (IGG)
- INSA: Interstellar Navigation & Security Authority
- Gatewatch
- Galactic Defense Force (GDF)
- Galactic Standard Credit (GSC)
- UniNet
- Crownspire
- Ironstar Forge
- Verdance
- Zero Space
- Riftspace
- Adaptive Systems Architecture (ASA), commonly Adaptive Technology

Important distinctions:

- IGG is a non-sovereign treaty authority/service framework, not a sixth nation.
- The Null is a cosmic/existential mystery, not the name of 0.0 frontier space.
- Zero Space is persistent Known Space with 0.0 security.
- Riftspace is outside the 200-system Known Space count.
- "Unknown Space" remains a broader conceptual/scientific term and must not be
  globally replaced.
- Compatibility-sensitive old identifiers may remain until a deliberate tested
  migration is approved.
- The old starter-hull name from an external setting is retired as current
  lore-facing canon; do not invent a final replacement without approval.

## 6. AUTHORITATIVE KNOWN SPACE MODEL

Known Space contains exactly **200 persistent systems**:

- Helion Commonwealth: 27
- Solar Directorate: 25
- Free Systems Compact: 25
- Meridian League: 23
- Aurelian Synod: 20
- Faction-controlled total: 120
- Zero Space: 80
- Total Known Space: 200

Riftspace, broader Unknown Space, and Deep Unknown Space are outside this count.

An older interim Batch 14 stage used an approximately-90-system implementation
target. That is historical staging only and must not be treated as the final
universe size or Batch 14 completion target.

## 7. GALACTIC MODEL

Use one shared authoritative topology model.

Keep independent:

1. space domain;
2. security classification;
3. sovereignty;
4. structure ownership;
5. discovery/knowledge state where modeled.

System security uses 0.2 increments.

Major sovereign NPC faction territory may contain High and Low Security but
does not use 0.0 as ordinary national territory.

Future player sovereignty belongs only in eligible Zero Space.

A structure owner does not automatically own the system.

Riftspace has no ordinary system-level sovereignty.

Zero Space geography should form coherent corridors, pockets, basins,
chokepoints, remote clusters, faction-border wilderness, backwaters, dangerous
alternatives, and dead ends rather than a uniform ring.

## 8. IGG

IGG is an independent treaty authority and interstellar service framework.

It is not:
- a sixth sovereign faction;
- a territorial government;
- owner of faction territory;
- owner of Zero Space;
- ordinary domestic police.

Its coequal arms are:
- INSA;
- Gatewatch;
- GDF.

Faction governments retain normal civil sovereignty and domestic law
enforcement.

IGG authority is mandate-based through treaty rights, Gate stewardship,
transnational warrants, emergency authority, and defined joint-security powers.

Represent IGG with the organization/institution model, not by forcing it into a
territorial-faction enum.

## 9. ECONOMY

HELION evolves toward a persistent player-influenced sandbox economy that can
also function at low player population.

Use GSC as the canonical user-facing currency.

Distinguish:
- Faucet: GSC created by the simulation.
- Sink: GSC removed from circulation.
- Transfer: existing GSC moved between actors.

Physical goods have locations.

Scarcity should emerge from geography, extraction, production, consumption,
destruction, logistics, security, conflict, infrastructure, events, and player
behavior.

NPC seeding is bootstrap infrastructure, not infinite lore supply.

TEST convenience prices are non-canonical.

Economic telemetry should support balancing, operations, exploit detection,
and UniNet/world-event feedback.

## 10. SHIPS / MODULES

Maintain separation among:

- canonical Ship Definition;
- Owned Ship Instance;
- owned module/inventory item;
- fitted module state;
- operator/visual variant;
- livery/wear;
- future modifications.

Do not mutate canonical hull definitions to represent one commander's ship.

Manufacturer identity and operator identity are separate.

Approved future heavy role-family names:

Combat/strategic:
- Breaker
- Aerie
- Grand Aerie
- Dominion

Industrial/logistics:
- Fleet Tender
- Foundry Tender
- Bulk Liner
- Farline Liner

These are future design direction, not proof of implemented ships.

## 11. PROTOCOL / SECURITY

TLS remains the transport-security boundary.

Protocol behavior must remain:
- bounded;
- deterministic;
- versioned;
- ABI-independent;
- safe under partial/coalesced input;
- rejecting malformed, truncated, oversized, invalid-version, invalid-type,
  and invalid-flag data;
- compatible with existing validated behavior unless a tested change is
  necessary.

Binary modernization must reuse the same authoritative gameplay handlers.

## 12. SURVEY / KNOWLEDGE DIRECTION

Approved knowledge progression:

DETECTED -> CLASSIFIED -> LOCATED -> SURVEYED

Physical truth and viewer knowledge are separate.

A commander, fleet, Guild, Alliance, faction, or public registry may know only
a subset of server truth.

Permission-filtered read models should omit undiscovered information rather
than sending hidden truth and merely hiding it visually.

Survey information may become a persistent strategic/economic asset.

Do not expand this into unrelated Batch 15 gameplay during Batch 14.

## 13. LIVING-UNIVERSE CANON — DRAFTCOMP7

DraftComp7 adds approved working-canon social density:

- current political leaders;
- current IGG leadership;
- military officers and service characters;
- historical figures across the nine-century recovery;
- famous civilian pilots and crews;
- notorious criminals;
- mysterious people and unresolved cases;
- cults, sects, mutual-aid orders, and strange movements;
- professional spacer guilds and associations;
- famous fleets/formations;
- corporate executives, boards, and internal factions;
- consumer brands and everyday service businesses;
- advertisers and popular culture;
- UniNet outlets and media personalities;
- ordinary station workers and families;
- crew traditions and shipboard social life;
- broader societal histories.

These are canon/worldbuilding assets, not an instruction to implement every one
during Batch 14.

IMPORTANT SCOPE RULE:
Do not expand Batch 14 simply because a new leader, cult, company, service,
fleet, media outlet, or historical character now exists in DraftComp7.

Use these names when relevant to:
- current lore/docs;
- generated flavor text;
- mission/story hooks already in scope;
- UniNet content already in scope;
- sample data that needs a canonical identity.

Otherwise preserve them as future content hooks.

Protected cosmic mysteries remain protected.

## 14. LIVING WORLD / UNINET

UniNet is infrastructure, not one omniscient editorial voice.

The setting may include multiple media organizations, official feeds,
journalists, corporate communications, guild bulletins, and rumor networks.

A useful living-world event pattern is:

1. verified event;
2. official statement;
3. journalistic report;
4. expert interpretation;
5. local witness;
6. rumor/propaganda;
7. gameplay consequence.

Do not have news copy casually reveal protected mysteries.

## 15. PROFESSIONAL / COMMERCIAL SOCIETY

HELION's spacer culture is distributed among overlapping licensing systems,
guilds, insurers, employers, local authorities, mutuals, and professional
associations.

Do not create one universal pilot super-guild that controls all independent
commanders unless explicitly approved later.

Commercial and civilian services matter because people eat, sleep, work,
repair ships, insure cargo, send money, hire crews, consume entertainment,
advertise, litigate, mourn, and raise families.

Use DraftComp7's brands and services as world texture where appropriate, but do
not convert flavor content into mandatory infrastructure without a design need.

## 16. ENVIRONMENTS

DEVELOPMENT:
- local/configurable;
- disposable;
- automated tests;
- debug/developer overrides.

PRIVATE TEST:
- isolated from production;
- developers + explicitly invited testers;
- separate state/config/credentials/logs/backups;
- may be reset/reseeded/rolled back;
- progress is non-canonical;
- developer mutation commands are restricted to authorized DEV_ADMIN roles.

PRODUCTION:
- one canonical persistent public universe;
- state persists independently of connected clients;
- continuous operation except scheduled maintenance/operator shutdown;
- production administration uses a separate least-privilege control plane.

Never allow writable-state crossover among environments.

## 17. COMPANION / API DIRECTION

Future clients may include:
- HELION Commander;
- HELION Terminal;
- HELION Flight Deck.

They must use shared authoritative service/read-model boundaries.

They must not become separate gameplay backends or independent authorities.

## 18. ORIGINAL-IP RULE

HELION may learn structurally from other sandbox/MMO/science-fiction designs,
but maintained canon must use HELION-native:
- names;
- factions;
- institutions;
- organizations;
- corporations;
- ship roles;
- technology;
- geography;
- news/media;
- economy language;
- visual identity;
- history.

Do not restore superseded or external-setting terminology because old code or
old prompts contain it.

## 19. CURRENT BATCH 14 COMPLETION DIRECTION

Batch 14 is a continuation, not a restart.

Preserve all validated Batch 13 and Batch 14 work already present.

Current completion direction includes:

1. forensic audit of current repository state;
2. finish genuinely incomplete interrupted protocol/client/server/TLS work;
3. authoritative exact 200-system Known Space;
4. meaningful 80-system Zero Space geography;
5. minimum coherent non-sovereign IGG representation;
6. clean separation of domain/security/sovereignty/ownership;
7. compatibility/persistence/query/protocol reconciliation;
8. GSC/UniNet/current canon documentation reconciliation;
9. future Riftspace not architecturally blocked;
10. focused + full relevant regression/build/security/package validation;
11. update execution evidence and current documentation;
12. produce Batch 14 completion report;
13. STOP before Batch 15.

The living-universe additions in DraftComp7 do not add new mandatory Batch 14
subsystems.

## 20. COMPLETION EVIDENCE

For substantial work, report:
- files changed;
- migrations;
- protocol changes;
- tests added;
- tests executed;
- exact results;
- regressions found/fixed;
- current Git status;
- current HEAD;
- remaining warnings;
- deferred work;
- whether the milestone acceptance criteria are fully satisfied.

Use concrete builds/tests/artifacts as evidence, not narrative confidence.