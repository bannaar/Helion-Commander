package com.example.helion.core.network

import com.example.helion.core.model.CommanderProfile
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.ServerEndpoint
import com.example.helion.core.model.ServerEnvironment
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompanionAuthenticationRequiredException(message: String) : Exception(message)
class CompanionAuthenticationException(message: String) : Exception(message)
class NativeProfileProtocolException(message: String) : Exception(message)

interface NativeCommanderProfileClient {
    suspend fun fetchProfile(
        environment: ServerEnvironment,
        endpoint: ServerEndpoint,
        companionToken: String
    ): Result<CommanderProfile>
}

class TlsNativeCommanderProfileClient(
    private val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory,
    private val connectTimeoutMs: Int = 5_000,
    private val readTimeoutMs: Int = 5_000
) : NativeCommanderProfileClient {

    override suspend fun fetchProfile(
        environment: ServerEnvironment,
        endpoint: ServerEndpoint,
        companionToken: String
    ): Result<CommanderProfile> = withContext(Dispatchers.IO) {
        try {
            requireValidCompanionToken(companionToken)
            val profile = Socket().use { tcpSocket ->
                tcpSocket.connect(
                    InetSocketAddress(endpoint.host, endpoint.port),
                    connectTimeoutMs
                )

                val tlsSocket = socketFactory.createSocket(
                    tcpSocket,
                    endpoint.host,
                    endpoint.port,
                    true
                ) as? SSLSocket
                    ?: throw ServerUnavailableException("TLS socket factory did not create an SSL socket.")

                tlsSocket.use { socket ->
                    socket.soTimeout = readTimeoutMs
                    val allowedProtocols = listOf("TLSv1.3", "TLSv1.2")
                        .filter { it in socket.supportedProtocols }
                    if (allowedProtocols.isEmpty()) {
                        throw ServerUnavailableException("TLS 1.2 or newer is unavailable on this device.")
                    }
                    socket.enabledProtocols = allowedProtocols.toTypedArray()

                    val sslParameters = socket.sslParameters
                    sslParameters.endpointIdentificationAlgorithm = "HTTPS"
                    socket.sslParameters = sslParameters
                    socket.startHandshake()

                    val welcome = readNativeLine(socket.inputStream)
                    parseNativeServerWelcome(environment, welcome)

                    val info = readNativeLine(socket.inputStream)
                    if (!info.startsWith("INFO commands=")) {
                        throw NativeProfileProtocolException("HELION server did not advertise its command surface.")
                    }

                    writeNativeLine(socket, "COMPANION AUTH $companionToken")
                    val auth = readResponse(socket.inputStream) { line ->
                        line.startsWith("OK COMPANION AUTH ") || line.startsWith("ERR ")
                    }
                    if (auth.startsWith("ERR ")) {
                        throw CompanionAuthenticationException(
                            when {
                                auth.startsWith("ERR companion-token-expired") ->
                                    "Companion credential has expired. Issue a new token from a normal player session."
                                auth.startsWith("ERR invalid-companion-token") ->
                                    "Companion credential was rejected by the HELION server."
                                auth.startsWith("ERR scope-denied") ->
                                    "Companion credential does not have the required profile.read scope."
                                else -> "HELION server rejected companion authentication."
                            }
                        )
                    }
                    if (!auth.contains(" scope=profile.read")) {
                        throw CompanionAuthenticationException(
                            "HELION server authenticated the credential without the required profile.read scope."
                        )
                    }

                    writeNativeLine(socket, "PROFILE")
                    val profileLine = readResponse(socket.inputStream) { line ->
                        line.startsWith("PROFILE ") || line.startsWith("ERR ")
                    }
                    if (profileLine.startsWith("ERR ")) {
                        throw NativeProfileProtocolException("HELION server rejected the PROFILE read.")
                    }

                    // Best-effort polite close. Failure here does not invalidate the verified profile read.
                    runCatching { writeNativeLine(socket, "QUIT") }
                    parseNativeCommanderProfile(environment, profileLine)
                }
            }
            Result.success(profile)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (known: CompanionAuthenticationException) {
            Result.failure(known)
        } catch (known: NativeProfileProtocolException) {
            Result.failure(known)
        } catch (known: ServerProtocolMismatchException) {
            Result.failure(known)
        } catch (known: ServerUnavailableException) {
            Result.failure(known)
        } catch (error: IllegalArgumentException) {
            Result.failure(CompanionAuthenticationException(error.message ?: "Invalid companion credential."))
        } catch (error: Exception) {
            Result.failure(
                ServerUnavailableException(
                    "Unable to read HELION commander profile from ${endpoint.displayAddress}.",
                    error
                )
            )
        }
    }
}

private const val NATIVE_PROFILE_MAX_LINE_BYTES = 4096
private const val NATIVE_PROFILE_MAX_RESPONSE_LINES = 8
const val NATIVE_PROFILE_NOT_ADVERTISED = "NOT ADVERTISED"

private val companionTokenRegex = Regex("^hc1\\.[0-9a-f]{16}\\.[0-9a-f]{64}$")

fun isValidCompanionTokenFormat(token: String): Boolean = companionTokenRegex.matches(token)

private fun requireValidCompanionToken(token: String) {
    require(isValidCompanionTokenFormat(token)) {
        "Companion credential format is invalid."
    }
}

private fun writeNativeLine(socket: SSLSocket, line: String) {
    require(line.length <= NATIVE_PROFILE_MAX_LINE_BYTES && !line.contains('\n') && !line.contains('\r')) {
        "Native request line is invalid."
    }
    socket.outputStream.write((line + "\n").toByteArray(StandardCharsets.UTF_8))
    socket.outputStream.flush()
}

private fun readResponse(input: InputStream, accept: (String) -> Boolean): String {
    repeat(NATIVE_PROFILE_MAX_RESPONSE_LINES) {
        val line = readNativeLine(input)
        if (accept(line)) return line
    }
    throw NativeProfileProtocolException("HELION server response did not contain the expected record.")
}

private fun readNativeLine(input: InputStream): String {
    val bytes = ByteArrayOutputStream()
    while (true) {
        val value = input.read()
        if (value == -1) {
            throw ServerUnavailableException("HELION server closed the TLS connection unexpectedly.")
        }
        if (value == '\n'.code) break
        if (bytes.size() >= NATIVE_PROFILE_MAX_LINE_BYTES) {
            throw NativeProfileProtocolException("HELION server response exceeded the 4096-byte protocol limit.")
        }
        if (
            value == 0 ||
            (value < 0x20 && value != '\t'.code && value != '\r'.code) ||
            value == 0x7f
        ) {
            throw NativeProfileProtocolException("HELION server response contained an invalid control byte.")
        }
        bytes.write(value)
    }
    val line = bytes.toString(StandardCharsets.UTF_8.name())
    return if (line.endsWith("\r")) line.dropLast(1) else line
}

private val nativeProfileRegex = Regex(
    "^PROFILE user=([^\\s]+) display=(.*?) faction=([^\\s]+) ship=([^\\s]+) " +
        "credits=([0-9]+) experience=([0-9]+) hull=([0-9]+) max-hull=([0-9]+) " +
        "engine-level=([0-9]+) hull-level=([0-9]+) mission-stage=([0-9]+) mission-ore-mined=([01])$"
)

fun parseNativeCommanderProfile(
    environment: ServerEnvironment,
    profileLine: String
): CommanderProfile {
    val match = nativeProfileRegex.matchEntire(profileLine)
        ?: throw NativeProfileProtocolException("Malformed HELION native PROFILE record.")

    val user = match.groupValues[1]
    val display = match.groupValues[2]
    val ship = match.groupValues[4]
    val credits = match.groupValues[5].toLongOrNull()
        ?: throw NativeProfileProtocolException("Malformed PROFILE credits value.")
    val experience = match.groupValues[6].toLongOrNull()
        ?: throw NativeProfileProtocolException("Malformed PROFILE experience value.")

    return CommanderProfile(
        commanderId = user,
        displayName = display,
        callSign = NATIVE_PROFILE_NOT_ADVERTISED,
        credits = credits,
        xp = experience,
        rank = NATIVE_PROFILE_NOT_ADVERTISED,
        career = NATIVE_PROFILE_NOT_ADVERTISED,
        currentSystemId = "",
        currentSystemName = NATIVE_PROFILE_NOT_ADVERTISED,
        currentStationId = "",
        currentStationName = NATIVE_PROFILE_NOT_ADVERTISED,
        activeShipId = ship,
        factionStandings = emptyList(),
        licenses = emptyList(),
        guildId = null,
        guildTicker = null,
        allianceId = null,
        environment = HelionEnvironment.fromServerEnvironment(environment),
        // Native protocol v2 does not advertise authoritative server time.
        serverTimeEpoch = 0L
    )
}
