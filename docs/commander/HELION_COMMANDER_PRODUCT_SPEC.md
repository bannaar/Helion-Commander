# HELION COMMANDER PRODUCT SPEC

## Purpose
A phone/tablet companion for away-from-client access to commander information, fleet planning, routing, bounded market information, UniNet, organizations, communications, and companion-safe services.

## Core product principles
1. Read-first/read-mostly.
2. Local planning is not server truth.
3. Mutations are requests validated by the HELION server.
4. Offline mode may show cached data and local plans but must not pretend to complete server transactions.
5. DEMO, PRIVATE_TEST, and PRODUCTION are isolated.
6. Mock/demo provenance must be visible during development.

## Safe local state
- hypothetical loadout plans
- route plans/bookmarks
- market watchlists/alerts
- UI preferences
- cached already-authorized data

## Do not expose as ordinary companion powers
- direct market-price mutation
- simulated ship damage/wear
- weapons/combat decisions
- automated mining
- arbitrary GSC/asset transfer
- unrestricted market automation
- sovereignty mutation
- developer/admin commands

## Screen direction
Home: commander/ship/location summaries, route, watched markets, UniNet, service status.
Commander: identity, portrait, ranks/XP, standings, licenses, permitted memberships, GSC.
Fleet: distinguish canonical hull definitions, owned instances, fitted modules, inventory, local fitting plans, liveries.
Universe: display only authorized topology/knowledge. Route labels use Zero Space.
Market: watch/compare/plan. Real orders require verified server support.
UniNet: multiple sources/voices; no omniscient mystery spoilers.
Guild/Alliance: membership/role filtered. Structure ownership != system sovereignty.
Comms: security claims must match actual implementation.
Settings: clearly distinguish target profile, connection state, cache state, and data provenance.

## Prototype rule
Continue this codebase. Do not wholesale rewrite it. Repair incrementally with tests.
