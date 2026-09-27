# AI STUDIO INSTRUCTIONS — HELION COMMANDER

Status: authoritative AI-development instructions for this repository.

## Mission
Develop HELION Commander as a non-authoritative companion application for HELION. Preserve useful prototype work and correct it incrementally.

## Authority order
1. Verified HELION server/API behavior.
2. This repository's source and tests.
3. docs/commander/HELION_COMMANDER_PRODUCT_SPEC.md
4. docs/commander/HELION_COMMANDER_CONTEXT.md
5. docs/commander/HELION_COMMANDER_SERVER_INTEGRATION.md
6. docs/project/HELION_ACTIVE_GOAL_v0.20.md
7. docs/project/HELION_PROJECT_STATE_HANDOFF_v0.20.md
8. docs/canon/HELION_World_Bible_draftcomp7.txt
9. docs/canon/HELION_CANON_CHANGELOG_AI_SYNC_v0.20.txt
10. docs/canon/HELION_CANON_AND_DOC_RECONCILIATION_v0.20.md

## Non-negotiable rules
- The HELION server owns authoritative gameplay truth.
- Commander may display, cache, plan, bookmark, simulate locally, and submit bounded requests.
- Do not create a second authoritative economy, inventory, galaxy, account, mission, ship, Guild, or sovereignty backend.
- FakeCompanionApi is DEMO/MOCK data, never proof of server implementation.
- RealCompanionApi must not implement DevelopmentSimulationApi.
- Do not expose hidden server truth to the client and then rely on UI hiding it.
- Keep DEMO, PRIVATE_TEST, and PRODUCTION state isolated.
- Keep secrets out of Git.
- Preserve compatibility-sensitive internal identifiers until a deliberate migration is tested.

## Current player-facing terminology
Use: IGG, INSA, Gatewatch, GDF, GSC, UniNet, Crownspire, Ironstar Forge, Verdance, Zero Space, Riftspace, ASA.

Do not restore current-facing: Helion Concordat, GalNet, generic Credits, Concordia as Commonwealth capital, Titan Forge, New Eden, Null-Sec/Null Space as the 0.0 region name.

Internal legacy identifiers such as galnet, credits, NULL_SECURITY, AVOID_NULLSEC, and sys-concordia may remain temporarily if changing them risks compatibility. Their UI labels must use current canon.

## Universe rules
Known Space is exactly 200 systems: 120 faction-controlled and 80 Zero Space.
Faction allocation: 27 / 25 / 25 / 23 / 20.
IGG is non-sovereign.
Aurelia is an Aurelian Synod center, not ordinary player-owned 0.0 space.
Zero Space is 0.0 Known Space and is not The Null.
Riftspace is outside the 200-system count.

## Verified native transport facts

Current HELION native-server status behavior is verified against `bannaar/Helion` `main` at `3b7fd52bb1fd36d6933eff0bbff35a3138c22568`:

- raw TLS transport, TLS 1.2+ required;
- default native port 4242;
- native protocol version 2;
- initial greeting `WELCOME Helion/2`;
- no verified REST/WebSocket transport for Commander;
- the greeting does not advertise a server software version or maintenance state;
- `STATE` exists pre-auth but is not required for status probing.

Do not replace these facts with assumptions from the proposed API document.

## Before editing
Audit whether each feature is VERIFIED SERVER, LOCAL/CACHED, MOCKED, PLANNED, or UNKNOWN. Never silently upgrade a mock into a production claim.
