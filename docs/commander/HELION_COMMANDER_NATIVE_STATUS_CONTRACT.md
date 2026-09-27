# HELION COMMANDER NATIVE STATUS CONTRACT

Status: verified implementation contract for the first real HELION server integration.

## Verified source

Verified against the current `bannaar/Helion` repository on `main` at commit:

```text
3b7fd52bb1fd36d6933eff0bbff35a3138c22568
```

Relevant native server files:

```text
native/server/main.cpp
native/shared/protocol.h
native/shared/protocol.cpp
native/tests/protocol_tests.cpp
native/tests/server_tests.cpp
```

The native server foundation itself was introduced by commit `da1b38e` and remains present on the verified current `main`.

## Verified transport

The native server uses a raw newline-delimited protocol over TLS.

Verified properties:

- default native port: `4242`
- TLS is required; there is no plaintext fallback
- server/client implementation requires TLS 1.2+
- protocol version: `2`
- initial server greeting: `WELCOME Helion/2`
- the server then emits an `INFO commands=...` line
- `STATE` exists as a pre-auth command, but it reports server data counts and is not required for a status/compatibility probe

The Commander status probe intentionally stops after the authenticated TLS handshake and compatible `WELCOME Helion/2` greeting.

## What the current native server does not advertise

The verified greeting does not contain:

- server software/build version
- maintenance state
- environment identity
- REST endpoint
- JSON schema
- WebSocket endpoint

Commander therefore represents server software version and maintenance as unknown for this probe. It must not invent values.

## Commander implementation

`TlsNativeServerStatusProbe`:

1. opens an SSL socket;
2. permits TLS 1.2 or TLS 1.3 only;
3. enables certificate hostname/identity verification;
4. connects to the configured host/port;
5. performs the TLS handshake;
6. reads the first line;
7. requires `WELCOME Helion/2`;
8. reports a successful native-server status only if all checks pass.

A mismatched protocol version fails with `ServerProtocolMismatchException`.

An unexpected greeting or connection/TLS failure returns a failed status probe.

## Endpoint configuration

Endpoints are build-time configuration, not hardcoded source values.

Supported environment variables:

```text
HELION_PRIVATE_TEST_HOST
HELION_PRIVATE_TEST_PORT
HELION_PRODUCTION_HOST
HELION_PRODUCTION_PORT
```

Ports default to `4242` when the corresponding host is configured and no valid port is supplied.

An empty host means that environment remains `NOT CONFIGURED`.

## Certificate trust

The current Commander probe uses the Android/platform TLS trust store and verifies that the certificate identity matches the configured host.

A local or private server using an untrusted development/self-signed certificate will not be accepted automatically.

Do not disable certificate verification to make a private-test connection work. Add an explicit trusted-certificate strategy in a future bounded security change if private-test deployment requires it.

## Scope boundary

This status integration does not implement:

- login/authentication
- account creation
- commander profile reads
- fleet reads
- market reads or writes
- UniNet
- missions
- Guild data
- game mutations

Those remain separate milestones and must be verified against the native HELION server before implementation.
