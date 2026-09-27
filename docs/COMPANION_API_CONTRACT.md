# HELION Companion API Contract Specification

**Document Version:** 1.0.1-PROPOSED  
**Target Backend:** HELION Persistent Universe Server  
**Transport Protocol:** PROPOSED ONLY — verify against the actual HELION server before implementation  
**Encoding:** PROPOSED ONLY — not yet an implementation fact  
**Authentication:** PROPOSED ONLY — use scoped, revocable credentials when the server contract is defined

---

> **STATUS WARNING:** Every endpoint, transport, authentication mechanism, and event in this document is a design proposal until verified against the main HELION server repository and runtime. This file must not be used as evidence that an endpoint exists. See `docs/commander/HELION_COMMANDER_API_RECONCILIATION.md`.

## 1. Core Architectural Principle

All endpoints must be strictly **authoritative**.  
The Android companion application is an untrusted client. Any mutation capability must be explicitly supported and validated by the authoritative HELION server.  
The HELION server validates permissions, spatial proximity, station docking state, commodity inventory, and GSC balances prior to committing any state transition.

---

## 2. Proposed Endpoints

### 2.1 Authentication & Environment Session
- `POST /v1/auth/token`
  - Body: `{ "grantType": "refresh_token|password", "clientId": "helion-android-companion", ... }`
  - Returns: `{ "accessToken": "...", "expiresIn": 3600, "environment": "PRODUCTION|PRIVATE_TEST|DEV" }`

### 2.2 Commander Telemetry
- `GET /v1/commander/profile`
  - Returns: `CommanderProfileDTO` (commanderId, callSign, GSC, rank, xp, factionStandings, licenses, currentSystemId, currentStationId, activeShipId)

### 2.3 Fleet & Outfitting
- `GET /v1/fleet/ships`
  - Returns: `List<OwnedShipInstanceDTO>` (instanceId, hullDefinition, condition, cargo, installedModules, livery)
- `POST /v1/fleet/ships/{instanceId}/activate`
  - Validates: Commander must be docked at the station where the target ship is located.
  - Returns: `OwnedShipInstanceDTO`
- `POST /v1/fleet/ships/{instanceId}/refit`
  - Body: `{ "plannedModules": { "slotId": "moduleId" } }`
  - Validates: Commander docked at outfitting gantry, GSC balance >= total module cost, slot sizing compliance.
  - Returns: Authoritative updated `OwnedShipInstanceDTO` and new GSC balance.
- `POST /v1/fleet/ships/{instanceId}/livery`
  - Body: `{ "liveryId": "liv-tactical-cyan" }`
  - Returns: `OwnedShipInstanceDTO`

### 2.4 Universe Navigation Network
- `GET /v1/universe/systems`
  - Returns: `Map<String, SystemNodeDTO>` containing star positions, security ratings, sovereignty, and starlane gate connections.

### 2.5 Regional Markets & Economic Commits
- `GET /v1/markets/stations/{stationId}`
  - Returns: `List<MarketItemDTO>` (commodityId, buyPrice, sellPrice, stockUnits, demandUnits, lastUpdated)
- `POST /v1/markets/orders`
  - Body: `{ "stationId": "...", "commodityId": "...", "quantity": 16, "action": "BUY|SELL" }`
  - Validates: Spatial location (docked), market stock >= requested, commander balance >= total cost, cargo capacity >= requested.
  - Returns: `MarketTransactionResultDTO` (`transactionId`, `authoritativeCredits`, `authoritativeCargoUnits`, `committedPrice`)

### 2.6 UniNet News
- `GET /v1/uninet/articles?channel={category}` (candidate future route; legacy internal names may remain until migrated)
  - Returns: `List<UniNetArticleDTO>`

### 2.7 Guild & Alliance Territory
- `GET /v1/guilds/me`
  - Returns: `GuildInfoDTO` (guildId, ticker, grantedPermissions, memberList, sovereignTerritories, notices)
- `POST /v1/guilds/notices`
  - Body: `{ "title": "...", "body": "..." }`
  - Validates: User possesses `guild.notice.create` permission.

### 2.8 Comms & Messaging
- `GET /v1/comms/conversations`
  - Returns: `List<ConversationDTO>`
- `GET /v1/comms/conversations/{id}/messages`
  - Returns: `List<MessageDTO>`
- `POST /v1/comms/conversations/{id}/messages`
  - Body: `{ "body": "..." }`
  - Returns: `MessageDTO`

---

## 3. Real-Time WebSocket Events (Planned for Phase 2)

Endpoint: `WSS /v1/events`
Supported event types:
- `TELEMETRY_LOCATION_CHANGED`: Player warped or jumped in the main game.
- `MARKET_PRICE_UPDATED`: Volatile price change on watched commodities.
- `COMMS_MESSAGE_RECEIVED`: Real-time incoming direct or guild message.
- `UNINET_FLASH_BULLETIN`: Breaking sector alert.
