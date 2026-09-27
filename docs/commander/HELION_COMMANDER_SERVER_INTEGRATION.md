# HELION COMMANDER SERVER INTEGRATION

## Contract
The persistent HELION server is authoritative. Commander is an untrusted client.

## Current state
This repository supports multi-environment server profiles via `ServerEnvironment` (`DEMO`, `PRIVATE_TEST`, `PRODUCTION`).
- `DEMO` routes to `FakeCompanionApi` for local mock and offline development.
- `PRIVATE_TEST` and `PRODUCTION` route to `RealCompanionApi`. When unconfigured, they fail honestly with `ServerNotConfiguredException` and do not fall back to mock data.
- Verified real-server operations now include the raw native TLS status/protocol probe and scoped companion authentication for live `PROFILE` reads. Commander does not invent REST or WebSocket behavior.

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
   - **Credential Isolation:** `AuthCredentialStore` partitions companion bearer tokens strictly by environment namespace. Authoritative tokens are never stored for DEMO. TEST/Production tokens are AES-GCM encrypted with an AndroidKeyStore-held key and cryptographically bound to their environment as AAD.
   - **Database Cache Isolation:** `AppDatabase` maintains independent physical SQLite database files per environment (`helion_commander_demo.db`, `helion_commander_test.db`, `helion_commander_production.db`). Repository DAO providers resolve against the active environment at use time.
   - **Observer Isolation:** Long-lived Room observers cancel and re-subscribe when the selected environment changes, preventing a screen from remaining attached to a previous environment's DAO.
   - **In-Memory Isolation:** Commander, fleet, universe, market, UniNet, Guild, comms, and mission repository state is cleared on an environment transition before target-environment refreshes occur.
   - **Persistent Selection:** User selection persists via `EnvironmentPreferences`. `HelionAppContainer` is the runtime routing authority and updates the settings environment flow as part of the same local transition. The application does not silently switch or fallback environments if an endpoint is unreachable.
   - **Production Safeguard:** Switching into `PRODUCTION` requires explicit user confirmation via an alert dialog in `SettingsScreen`.
   - **Status Probing:** DEMO is labeled as a local simulation. Configured TEST/PRODUCTION profiles use the verified native TLS status probe. The current native server advertises protocol version `2` through `WELCOME Helion/2`, but does not advertise software version or maintenance state; those fields remain unknown. Unconfigured real servers report explicit unconfigured failure.
   - **Simulation Boundary:** DEMO development-simulation controls follow the delegated active API. They remain available in DEMO but are unavailable through ordinary PRIVATE TEST/PRODUCTION companion paths.

## M1 acceptance

M1 is not complete merely because the three database files exist. Validation must cover runtime switching through the same repository instances, re-subscription of long-lived Room flows, clearing of old in-memory data, Production confirmation, credential isolation, and the DEMO-only simulation boundary.

Required validation remains:

```text
gradle testDebugUnitTest
gradle assembleDebug
git diff --check
```

## Native status integration (M2)

The current HELION native server contract has been verified against `bannaar/Helion` `main` at `3b7fd52bb1fd36d6933eff0bbff35a3138c22568`.

Commander implements only the verified status/compatibility slice:

```text
TLS 1.2+
    ↓
certificate trust + hostname verification
    ↓
WELCOME Helion/2
    ↓
protocol-compatible status
```

It intentionally does not send the pre-auth `STATE` command because the greeting is sufficient for compatibility/status and `STATE` exposes profile/message counts.

See `HELION_COMMANDER_NATIVE_STATUS_CONTRACT.md`.

## Scoped companion authentication and PROFILE reads (M3)

Verified against `bannaar/Helion` `main` at `23cf90b0b7bdd0996c513f610fab21dc9a4af2e0`.

The server supports a dedicated 30-day, revocable `profile.read` bearer credential. It is issued from a normal password-authenticated HELION session with `COMPANION ISSUE`; Commander stores only the bearer token and never stores the game password.

Commander authenticates over the existing native TLS v2 connection:

```text
COMPANION AUTH <token>
PROFILE
QUIT
```

The current PROFILE record provides username, display name, faction identifier, ship identifier, credits, experience, hull/upgrade fields, and mission state. Commander maps only fields supported by its present read model and marks unsupported rich-profile fields `NOT ADVERTISED`.

Manual pairing is available in Settings for development builds. See `HELION_COMMANDER_COMPANION_AUTH_CONTRACT.md`.

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
Companion authorization uses scoped, revocable, expiring credentials rather than raw game passwords for the verified native PROFILE slice.
Player scopes never imply DEV_ADMIN, production operations, database, or security-admin access.

## Integration sequence
1. Server capability/status handshake — implemented.
2. Scoped companion authentication — implemented for `profile.read`.
3. Native commander PROFILE read — implemented with conservative field mapping.
4. Verify and implement owned-fleet reads.
5. Continue with permission-filtered universe/UniNet/market/mission surfaces one capability at a time.
6. Only enable mutations after the exact server operation and authorization scope are verified.
