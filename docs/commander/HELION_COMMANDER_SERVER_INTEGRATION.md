# HELION COMMANDER SERVER INTEGRATION

## Contract
The persistent HELION server is authoritative. Commander is an untrusted client.

## Current state
This repository supports multi-environment server profiles via `ServerEnvironment` (`DEMO`, `PRIVATE_TEST`, `PRODUCTION`).
- `DEMO` routes to `FakeCompanionApi` for local mock and offline development.
- `PRIVATE_TEST` and `PRODUCTION` route to `RealCompanionApi`. When unconfigured, they fail honestly with `ServerNotConfiguredException` and do not fall back to mock data.

## Multi-Environment Foundation (M1)
1. **Environment Profiles:**
   - `DEMO`: Local simulated Kepler cluster dataset. Safe for offline development and UI testing.
   - `PRIVATE_TEST`: Isolated staging universe (LAB-SEC-7) for verified test deployments.
   - `PRODUCTION`: Live persistent HELION universe (HELION-1).
2. **Interface and Routing:**
   - `CompanionApiFactory` manages API creation and profile descriptors.
   - `DelegatingCompanionApi` dynamically delegates to the active environment's API.
   - `RealCompanionApi` implements `CompanionApi` for real server connectivity. Crucial Invariant: `RealCompanionApi` MUST NOT implement `DevelopmentSimulationApi`.
3. **Environment Isolation Invariants:**
   - **Credential Isolation:** `AuthCredentialStore` partitions bearer tokens strictly by environment namespace (`token_demo`, `token_private_test`, `token_production`). Tokens never crossover between test and production.
   - **Database Cache Isolation:** `AppDatabase` maintains independent physical SQLite database files per environment (`helion_commander_demo.db`, `helion_commander_test.db`, `helion_commander_production.db`). Test state never leaks into production.
   - **Persistent Selection:** User selection persists via `EnvironmentPreferences`. The application does not silently switch or fallback environments if an endpoint is unreachable.
   - **Production Safeguard:** Switching into `PRODUCTION` requires explicit user confirmation via an alert dialog in `SettingsScreen`.
   - **Status Probing:** `ServerStatus` probe read model reports service name, server version, protocol version, and maintenance status. Unconfigured real servers report explicit unconfigured failure.

## API discipline
docs/COMPANION_API_CONTRACT.md is a proposal until each endpoint, authentication mechanism, DTO, and event is verified against the real server.

Do not hardcode OAuth, mTLS, TLS 1.3, REST, JSON, Protobuf, or WebSockets as implemented facts merely because the proposal mentions them.

## Interface split
CompanionApi is the ordinary player companion boundary.
DevelopmentSimulationApi is mock/test-only.

RealCompanionApi MUST NOT implement DevelopmentSimulationApi.

## Read models
Return purpose-specific permission-filtered DTOs. Do not send hidden server objects and trust the client to conceal fields.

## Markets
Commander may observe, compare, watch, and submit supported orders.
It never sets authoritative commodity prices.

## Universe
Routing uses only topology available to the caller.
Domain, security, sovereignty, structure ownership, and discovery state remain separate.

## Authorization
Future companion authorization should use scoped, revocable, expiring credentials rather than raw game passwords.
Player scopes never imply DEV_ADMIN, production operations, database, or security-admin access.

## Integration sequence
1. Verify main HELION server capability.
2. Define versioned DTOs/read models.
3. Implement RealCompanionApi for verified operations only.
4. Add integration tests.
5. Add explicit connection/provenance state to UI.
6. Only then enable production-target actions.
