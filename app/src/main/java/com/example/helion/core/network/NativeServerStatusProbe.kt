package com.example.helion.core.network

import com.example.helion.core.model.ServerEndpoint
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerStatus
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

const val HELION_NATIVE_PROTOCOL_VERSION = 2

class ServerProtocolMismatchException(message: String) : Exception(message)

interface NativeServerStatusProbe {
    suspend fun probe(
        environment: ServerEnvironment,
        endpoint: ServerEndpoint
    ): Result<ServerStatus>
}

class TlsNativeServerStatusProbe(
    private val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory,
    private val connectTimeoutMs: Int = 5_000,
    private val readTimeoutMs: Int = 5_000
) : NativeServerStatusProbe {

    override suspend fun probe(
        environment: ServerEnvironment,
        endpoint: ServerEndpoint
    ): Result<ServerStatus> = withContext(Dispatchers.IO) {
        try {
            val status = Socket().use { tcpSocket ->
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

                    val welcome = readNativeProtocolLine(socket.inputStream)
                    parseNativeServerWelcome(environment, welcome)
                }
            }
            Result.success(status)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (known: ServerProtocolMismatchException) {
            Result.failure(known)
        } catch (known: ServerUnavailableException) {
            Result.failure(known)
        } catch (error: Exception) {
            Result.failure(
                ServerUnavailableException(
                    "Unable to verify HELION native TLS server at ${endpoint.displayAddress}.",
                    error
                )
            )
        }
    }
}

private const val HELION_NATIVE_MAX_LINE_BYTES = 4096

private fun readNativeProtocolLine(input: InputStream): String {
    val bytes = ByteArrayOutputStream()
    while (true) {
        val value = input.read()
        if (value == -1) {
            throw ServerUnavailableException(
                if (bytes.size() == 0) {
                    "HELION server closed the connection before sending a welcome line."
                } else {
                    "HELION server closed the connection before terminating the welcome line."
                }
            )
        }
        if (value == '\n'.code) break
        if (bytes.size() >= HELION_NATIVE_MAX_LINE_BYTES) {
            throw ServerUnavailableException("HELION server greeting exceeded the 4096-byte protocol limit.")
        }
        if (
            value == 0 ||
            (value < 0x20 && value != '\t'.code && value != '\r'.code) ||
            value == 0x7f
        ) {
            throw ServerUnavailableException("HELION server greeting contained an invalid control byte.")
        }
        bytes.write(value)
    }

    val line = bytes.toString(StandardCharsets.UTF_8.name())
    return if (line.endsWith("\r")) line.dropLast(1) else line
}

fun parseNativeServerWelcome(
    environment: ServerEnvironment,
    welcomeLine: String
): ServerStatus {
    val match = Regex("^WELCOME Helion/(\\d+)$").matchEntire(welcomeLine.trim())
        ?: throw ServerUnavailableException(
            "Unexpected HELION server greeting. Expected 'WELCOME Helion/<protocol>'."
        )

    val protocolVersion = match.groupValues[1].toInt()
    if (protocolVersion != HELION_NATIVE_PROTOCOL_VERSION) {
        throw ServerProtocolMismatchException(
            "Incompatible HELION native protocol: server=$protocolVersion, commander=$HELION_NATIVE_PROTOCOL_VERSION."
        )
    }

    return ServerStatus(
        serviceName = "HELION Native Server",
        environment = environment,
        serverVersion = null,
        protocolVersion = protocolVersion.toString(),
        maintenance = null,
        message = "TLS handshake verified and native protocol welcome accepted."
    )
}
