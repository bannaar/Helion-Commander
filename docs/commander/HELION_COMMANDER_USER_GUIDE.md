# HELION Commander User Guide

**Application:** HELION Commander  
**Platform:** Android phones and tablets  
**Current status:** Development prototype / pre-server-integration alpha

---

## 1. What Is HELION Commander?

HELION Commander is the Android companion application for HELION.

It is designed to give commanders useful information and planning tools away from the main game client.

The application includes interfaces for:

- commander information
- fleet and ship planning
- navigation
- markets
- missions
- UniNet
- Guilds and Alliances
- communications
- settings and environment information

The current development build uses **simulated HELION data**.

It is not yet connected to the persistent HELION production server.

---

## 2. Development-Build Notice

The current Commander build is a prototype.

When you see:

- GSC balances
- ships
- missions
- market prices
- organizations
- systems
- messages
- news
- standings

that information is currently demonstration data unless a future build explicitly identifies it as live.

A simulated transaction does not change your real HELION account.

---

## 3. Main Navigation

HELION Commander adapts to device size.

Phones generally use bottom navigation.

Larger tablets may use a navigation rail.

Main areas include:

```text
Home
Commander
Fleet
Universe
Market
UniNet
Guild
Comms
Missions
Settings
```

---

## 4. Home Dashboard

Home provides a quick command overview.

Depending on the current build, it may show:

- commander name
- GSC balance
- active ship
- hull condition
- fuel
- cargo
- power
- current system
- security status
- planned route
- watched market items
- mission information
- UniNet headlines
- environment/service information

Current gameplay data is simulated.

---

## 5. Commander Profile

The Commander section displays character information such as:

- commander name
- callsign
- GSC balance
- career information
- rank
- XP progress
- faction standings
- permits
- endorsements
- organization membership

In a future connected build, this information will come from your HELION account.

---

## 6. Fleet

Fleet lets you inspect ships and experiment with configurations.

You may see:

- ship instances
- hull information
- condition
- cargo
- modules
- hardpoints
- utilities
- core systems
- optional modules
- mass
- power use
- liveries

### Fitting Plans

Fitting plans are hypothetical configurations.

```text
LOCAL PLAN ≠ COMMITTED SHIP STATE
```

A future connected version will send supported refit requests to the HELION server for validation.

---

## 7. Ship Appearance

The prototype includes livery and visual customization concepts.

You may preview things such as:

- paint patterns
- markings
- registration details
- weathering

A local preview is not automatically a server-owned cosmetic.

---

## 8. Universe

The Universe screen provides star-system and route-planning tools.

Features include:

- galaxy map
- system search
- system details
- starlane connections
- security information
- sovereignty information
- route calculation
- route bookmarks

Possible route policies include:

```text
Fastest
Safest
High-Sec Only
Avoid Low Security
Avoid Zero Space
Trade Corridor
```

Routes can show jump count, danger, security conditions, and border transitions.

---

## 9. Zero Space

Zero Space is 0.0-security Known Space.

It is not the same thing as **The Null**, which is a separate concept in HELION lore.

Use route policies when you want to avoid riskier space.

---

## 10. Market

The Market section provides regional trade-planning tools.

Prototype features include:

- local prices
- buy/sell rates
- stock
- demand
- price trends
- station comparisons
- profit calculations
- watchlists
- alerts

HELION currency is the:

# Galactic Standard Credit

Abbreviation:

```text
GSC
```

Current market data is simulated.

Commander does not control authoritative prices.

---

## 11. UniNet

UniNet is HELION's interstellar information infrastructure.

Commander may show categories such as:

- Top Stories
- Local
- Economy
- Security
- Conflict
- Exploration
- Community

You may also be able to mark stories read or bookmark them.

Current articles are demonstration content.

---

## 12. Guild and Alliance

The Guild area can display:

- Guild identity
- ticker
- member roster
- notices
- roles
- permissions
- organization information
- eligible territory information

Membership does not mean every member has every permission.

Future live permissions will be decided by the HELION server.

---

## 13. Sovereignty

Structure ownership and system sovereignty are separate.

A Guild can operate infrastructure without automatically controlling an entire star system.

Eligible player sovereignty belongs in appropriate Zero Space systems.

Major faction centers remain under their authoritative sovereign faction unless the HELION server says otherwise.

---

## 14. Comms

The Comms section contains prototype messaging interfaces.

Possible channel types include:

- direct messages
- Guild channels
- system-local channels
- trade channels

Current messages are simulated.

Do not assume labels such as “encrypted” imply a specific verified cryptographic implementation unless a future build documents it.

---

## 15. Missions

Commander contains tactical mission interfaces.

You may see:

- active missions
- objectives
- destinations
- rewards
- completion state
- mission statistics
- history charts

Current mission state and history are demonstration data.

---

## 16. Settings

The current prototype provides explicit environment selection:

```text
DEMO / OFFLINE
PRIVATE TEST
PRODUCTION
```

The environment badge identifies the active target.

- **DEMO / OFFLINE** uses local simulated data.
- **PRIVATE TEST** selects the isolated test namespace. Default builds have no endpoint configured; specially configured builds can perform the verified native TLS status probe.
- **PRODUCTION** selects the persistent-universe target namespace. Default builds have no endpoint configured; specially configured builds can perform the verified native TLS status probe.

Selecting PRIVATE TEST or PRODUCTION does not fabricate a connection. Their local caches, local planning state, and credential namespaces are isolated from each other and from DEMO. Switching into Production requires an explicit confirmation.

---

## 17. Offline Use

Offline-safe activities may include:

- reviewing cached information
- viewing fitting plans
- route planning
- reading saved articles
- using bookmarks
- managing watchlists
- adjusting UI preferences

Commander must not pretend an authoritative server action succeeded while offline.

---

## 18. Server Selection

Server selection is implemented as part of the Multi-Environment Server Foundation.

### DEMO / OFFLINE
Uses local simulated data and is clearly marked as a local simulation.

### PRIVATE TEST
Uses a separate TEST database/cache and credential namespace. If no TEST host is configured, it reports NOT CONFIGURED rather than falling back to DEMO. If an endpoint is configured, Commander can verify the server through a trusted TLS connection and the HELION native protocol-v2 greeting.

### PRODUCTION
Uses a separate Production database/cache and credential namespace. If no Production host is configured, it reports NOT CONFIGURED rather than falling back to DEMO. If an endpoint is configured, Commander can perform the same TLS/protocol compatibility check.

When you change environments, environment-scoped in-memory data is cleared and persistent observers rebind to the selected namespace so DEMO data is not shown as TEST or Production data.

---

## 19. TEST Versus Production

The private TEST universe is intended for development and controlled testing.

Future test assets may include:

- test GSC
- test ships
- test modules
- test inventory
- test progression
- test market conditions

TEST assets must not automatically transfer to Production.

Production represents the persistent HELION universe.

---

## 20. Developer Commands

Future developer/admin accounts on the PRIVATE TEST server may receive specialized controls such as:

- grant test GSC
- grant test ships/modules
- change test XP/rank
- modify test inventory
- change test standings
- move a test character
- reset test state

These are not ordinary-player features and are not intended for Production.

---

## 21. Data Provenance

Future builds may mark information as:

```text
LIVE
CACHED
LOCAL PLAN
MOCK
STALE
UNAVAILABLE
```

Meaning:

**LIVE**  
Received from the connected authoritative HELION service.

**CACHED**  
Previously received information stored locally.

**LOCAL PLAN**  
Your own hypothetical plan/bookmark.

**MOCK**  
Development/demo information.

**STALE**  
Information that may no longer match the server.

**UNAVAILABLE**  
Commander cannot currently obtain the information.

---

## 22. Current Prototype Limitations

The present development build can perform a verified native-server TLS/protocol status check when an endpoint is configured. It does not yet provide verified live:

- HELION server authentication
- Production account access
- PRIVATE TEST account access
- live commander profile
- live GSC
- live fleet state
- live inventory
- live market state
- live missions
- live Guild state
- live discovery state
- live UniNet service

These will be added incrementally.

---

## 23. Planned Integration Order

The intended integration sequence is:

```text
Server connection/status [implemented for configured native TLS endpoints]
        ↓
Authentication
        ↓
Commander profile
        ↓
Owned fleet
        ↓
Authorized universe information
        ↓
UniNet
        ↓
Markets
        ↓
Missions
        ↓
Safe server-validated actions
```

---

## 24. Quick Start for the Current Development Build

After installing a development APK:

1. Open HELION Commander.
2. Explore Home.
3. Open Commander to inspect the sample profile.
4. Open Fleet and experiment with fitting plans.
5. Open Universe and compare routes.
6. Open Market and compare simulated prices.
7. Browse UniNet.
8. Explore Guild and Comms.
9. Open Missions.
10. Review Settings.

Remember:

```text
DEFAULT MODE = DEMO / MOCK DATA
PRIVATE TEST = NOT CONFIGURED unless build supplies a native endpoint
PRODUCTION = NOT CONFIGURED unless build supplies a native endpoint
```

---

## 25. Troubleshooting

### Production is selected but my real account is missing
Environment selection and the status probe do not provide account login. Authentication and live commander/account reads are not implemented yet.

### My ships or GSC do not match HELION
The current build uses mock data.

### A market transaction appears to work
It is a prototype simulation unless a future release explicitly identifies it as live.

### Information looks old
It may be mock, cached, or stale.

### An authoritative action is unavailable offline
That is intentional. Commander must not fabricate successful server transactions.

---

## 26. Current Canon Reference

```text
Currency              GSC
Information network   UniNet
0.0 frontier          Zero Space
Treaty framework      IGG
Navigation/security   INSA
Gate service          Gatewatch
Defense force         GDF
Commonwealth capital  Crownspire
Industrial identity   Ironstar Forge
Lost world            Verdance
Unknown domain        Riftspace
Advanced technology   ASA
```

---

## 27. Summary

HELION Commander is already a substantial Android companion prototype with:

- commander management
- fleet planning
- navigation
- market tools
- missions
- organizations
- UniNet
- communications

It defaults to DEMO simulated data. PRIVATE TEST and PRODUCTION selection/isolation are implemented, but verified real server connections are not configured yet.

The governing rule is:

```text
COMMANDER SHOWS AND REQUESTS.

HELION SERVER DECIDES AND COMMITS.
```

As real server integration is added, Commander will transition from a development simulator into a genuine companion to the persistent HELION universe.
