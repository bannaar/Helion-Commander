# HELION COMMANDER SERVER INTEGRATION

## Contract
The persistent HELION server is authoritative. Commander is an untrusted client.

## Current state
This repository currently instantiates FakeCompanionApi. It is not connected to production, private-test, or development HELION servers.

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
