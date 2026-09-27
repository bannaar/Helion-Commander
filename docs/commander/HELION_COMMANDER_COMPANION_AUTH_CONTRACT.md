# HELION COMMANDER COMPANION AUTH CONTRACT

Status: verified native companion-authentication contract for HELION Commander.

## Verified source

Verified against `bannaar/Helion` `main` at:

```text
23cf90b0b7bdd0996c513f610fab21dc9a4af2e0
```

The server-side foundation is documented in `docs/companion-auth.md` in the HELION repository.

## Transport

Companion authentication uses the existing HELION native protocol over TLS:

```text
TLS 1.2+
WELCOME Helion/2
INFO commands=...
COMPANION AUTH <bearer-token>
PROFILE
QUIT
```

No REST, JSON, WebSocket, OAuth, or game-password storage is involved in this milestone.

## Credential model

The native server issues a bearer credential only from an already password-authenticated game session:

```text
COMPANION ISSUE
```

The resulting credential:

- is a random 256-bit bearer secret;
- has a public token ID;
- expires after 30 days;
- is revocable;
- has fixed initial scope `profile.read`;
- is persisted by the server only as a SHA-256 verifier;
- cannot perform gameplay mutations or credential escalation.

Commander stores only the bearer token. It never stores the player's HELION game password.

## Commander credential storage

`AuthCredentialStore`:

- partitions credentials by `ServerEnvironment`;
- rejects authoritative credential storage in DEMO/OFFLINE;
- encrypts the bearer with AES-GCM;
- keeps the AES key in AndroidKeyStore on production devices;
- authenticates the environment name as AES-GCM AAD;
- migrates the earlier placeholder plaintext preference to encrypted storage on first read.

TEST and Production credentials remain separate.

## Live PROFILE mapping

The current native server returns:

```text
PROFILE user=<user> display=<display> faction=<faction> ship=<ship>
        credits=<n> experience=<n> hull=<n> max-hull=<n>
        engine-level=<n> hull-level=<n> mission-stage=<n>
        mission-ore-mined=<0|1>
```

Commander maps only fields supported by its current `CommanderProfile` model:

- `user` -> `commanderId`
- `display` -> `displayName`
- `credits` -> `credits`
- `experience` -> `xp`
- `ship` -> `activeShipId` for this native-v2 compatibility slice

Fields not advertised by native protocol v2 are not fabricated:

- callsign
- rank
- career
- current system/station
- faction standing values
- licenses
- Guild/Alliance data
- authoritative server time

String fields use `NOT ADVERTISED` where the existing model requires a value.
The existing `serverTimeEpoch` field uses `0` as the native-v2 unknown sentinel.

## Manual development pairing

This milestone uses a manual pairing surface in Settings.

1. Log in through the normal native HELION client.
2. Run `COMPANION ISSUE`.
3. Copy the returned `hc1...` bearer token.
4. Select the matching PRIVATE TEST or PRODUCTION environment in Commander.
5. Paste the token into Companion Credential Pairing.
6. Save encrypted.
7. Use VERIFY PROFILE to authenticate and perform a live `PROFILE` read.

Do not paste a TEST token into Production or vice versa.

## Scope boundary

This milestone does not implement live:

- fleet ownership/details
- markets
- missions
- UniNet
- Guild/Alliance reads
- comms
- inventory
- gameplay mutations
- token issuance or revocation from Commander

Those remain separate verified-server milestones.
