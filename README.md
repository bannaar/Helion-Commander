# HELION Commander

Official companion application for **HELION**, the persistent, server-authoritative online science-fiction universe.

---

## 1. Project Purpose & Core Architecture Principle

HELION Commander is a tactical commander management, fleet outfitting, star system navigation, regional market analysis, and universe communications application designed to be used while away from the main game.

> **CRITICAL SERVER-AUTHORITY PRINCIPLE**  
> The Android client may **REQUEST** actions.  
> The HELION Universe Server **VALIDATES** and **COMMITS** them.  
> The Android client never independently creates client-owned truth for credits, inventory, ship ownership, fitted modules, market stock, market prices, faction standings, guild sovereign territory, or economic transactions.

Local planning states (such as hypothetical loadout plans, saved route plans, bookmarks, and market watchlists) are clearly distinguished from authoritative server-committed assets.

---

## 2. Package Organization

```
com.example.helion/
├── core/
│   ├── HelionAppContainer.kt       # Dependency injection & service locator
│   ├── model/                     # Pure domain models (Commander, Ship, Universe, Market, GalNet, Guild, Comms)
│   ├── network/                   # CompanionApi contract and FakeCompanionApi
│   ├── database/                  # Room Database, Entities, and Reactive DAOs
│   ├── navigation/                # Deterministic Dijkstra/A* galaxy router
│   └── repository/                # Repository layer abstracting local cache & server API
└── ui/
    ├── HelionApp.kt               # Adaptive navigation shell (BottomBar / NavigationRail)
    ├── components/                # Tactical scifi cards, stat bars, security pills, top bar
    ├── home/                      # Commander dashboard
    ├── commander/                 # Detailed commander profile, ranks, faction standings
    ├── fleet/                     # Owned ships, outfitting loadout planner, livery customizer
    ├── universe/                  # 2D Canvas galaxy map, system search, graph route planner
    ├── market/                    # Local markets, cross-system price arbitrage, buy/sell orders
    ├── galnet/                    # Galactic wire news, categorised channels, reader
    ├── guild/                     # Guild hub, member telemetry, sovereign territory, directives
    ├── comms/                     # Secure direct comms, guild broadcast, local system channels
    └── settings/                  # Environment switcher, offline mode simulation, cache management
```

---

## 3. Implemented Features (Phase 1)

1. **Adaptive Interface & Navigation:**
   - Handheld phone bottom navigation and large-screen / tablet `NavigationRail`.
   - Distinctive HELION visual identity: near-black graphite surfaces (`#070A10`, `#0C101A`), tactical cyan HUD glows (`#00E5FF`), hazard amber (`#FFB300`), security classification pills.
2. **Home Dashboard:**
   - Commander status, active ship readiness (Hull, Fuel, Cargo, Power), current system security, active planned flightpath, local market watch, breaking GalNet news, and universe server status.
3. **Commander Profile:**
   - Display name, callsign, credit balance, rank & XP progression, career path, faction standings with dynamic progress indicators, endorsements, and permits.
4. **Fleet & Ship Loadout Planner:**
   - Multi-ship persistent fleet management (separate instances: *Aster Raptor*, *Titan Mule*, *Commonwealth Valiant*).
   - Slot-level outfitting (Hardpoints, Utilities, Core Internals, Optional Internals) with real-time mass, power budget, and overload detection.
   - Save, load, and delete local fitting plans without altering physical ship fittings.
   - "Request Server Refit" sends validation request to server checking station docking, slot sizes, and credit balance.
5. **Ship Livery & Appearance:**
   - Paint patterns (Tactical Cyan, Void Shadow, Directorate Prestige, Hazard Striping), registration markings, and weathering preview slider.
6. **Universe Map & Graph Route Planner:**
   - Interactive 2D Canvas map with Starlane connections colored by classification (Trunk Gate, Regional Primary, Secondary, Frontier, Backwater).
   - Dijkstra pathfinding supporting multiple routing policies: *Fastest*, *Safest*, *High-Sec Only*, *Avoid Low-Sec*, *Avoid Null-Sec*, and *Trade Corridor*.
   - Route hop analysis, jump counts, lowest security on path, border transitions, and danger warnings.
   - Room-backed route bookmarks.
7. **Regional Market & Arbitrage:**
   - Local station market stock, demand, buy/sell rates, and price trend indicators.
   - Cross-station price comparison table with calculated margins per ton and cargo profit estimates.
   - Trade profit simulator based on customizable cargo capacity.
   - Authoritative Buy and Sell execution with server credit validation, cargo hold space limits, and stock decrements.
8. **GalNet News Wire:**
   - Channels: Top Stories, Local, Economy, Security, Conflict, Exploration, Community.
   - Read/unread tracking and bookmarking for offline review.
9. **Guild & Sovereignty Hub:**
   - Guild profile, online member rosters, 0.0 Null-Sec sovereign territories with daily revenue tariffs.
   - Gated action controls: directives/notices can only be posted if the server returns `guild.notice.create` permission.
10. **Universe Messenger:**
    - Channels: Direct encrypted comms, Guild command, and System local broadcasts.
    - Compose and send messages through simulated server transport.
11. **Environment Isolation & Offline Simulation:**
    - Live switching between `PRODUCTION`, `PRIVATE_TEST`, and `DEVELOPMENT`.
    - Simulated offline mode with graceful degradation: cached browsing remains functional, but sensitive economic and outfitting orders are safely blocked.

---

## 4. How to Run & Build

```bash
# Build the application APK
gradle assembleDebug

# Run JVM Unit and Architecture Tests
gradle testDebugUnitTest
```

---

## 5. Testing & Verification

Comprehensive unit tests cover:
- Graph pathfinding algorithms under different route policies (*High-Sec Only*, *Safest*, *Fastest*).
- Security classification logic across +5.0 to -5.0 and 0.0 null-sec.
- Market price spread and margin calculations.
- Server-authoritative validation rules (insufficient balance or missing permissions reject transactions).
- Offline mode blocking.
