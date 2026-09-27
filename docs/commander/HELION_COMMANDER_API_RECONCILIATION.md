# HELION COMMANDER API RECONCILIATION

The existing docs/COMPANION_API_CONTRACT.md is a proposed interface sketch, not an implementation report.

Do not infer that HELION currently implements OAuth 2.0, mTLS, TLS 1.3 specifically, JSON/Protobuf REST APIs, WebSocket telemetry, or any listed path without verifying the main server.

## Current corrections
- Ordinary CompanionApi no longer exposes direct mock market-price mutation.
- Ordinary CompanionApi no longer exposes mock ship-wear mutation.
- Those controls live under DevelopmentSimulationApi and must never be implemented by RealCompanionApi.
- Player-facing semantics use GSC, UniNet, and Zero Space.
- Legacy technical names may remain until migrated safely.
- Galaxy responses must be permission-filtered and discovery-aware.
- Guild membership does not imply access to all Guild intelligence.
- Structure ownership does not imply system sovereignty.
- Private-test admin/developer commands are outside ordinary player companion scope.

When real server integration begins, create docs/COMPANION_API_IMPLEMENTED.md from verified server behavior. Keep proposed and implemented contracts separate.
