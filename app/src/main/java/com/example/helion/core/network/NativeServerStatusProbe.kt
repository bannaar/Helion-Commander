package com.example.helion.core.network

import com.example.helion.core.model.ServerEndpoint
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerStatus
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
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
        runCatching {
            val rawSocket = socketFactory.createSocket()
            val socket = rawSocket as? SSLSocket
                ?: throw ServerUnavailableException("TLS socket factory did not create an SSL socket.")

            socket.use { tlsSocket ->
                tlsSocket.soTimeout = readTimeoutMs

                val allowedProtocols = listOf("TLSv1.3", "TLSv1.2")
                    .filter { it in tlsSocket.supportedProtocols }
                if (allowedProtocols.isEmpty()) {
                    throw ServerUnavailableException("TLS 1.2 or newer is unavailable on this device.")
                }
                tlsSocket.enabledProtocols = allowedProtocols.toTypedArray()

                val sslParameters = tlsSocket.sslParameters
                sslParameters.endpointIdentificationAlgorithm = "HTTPS"
                tlsSocket.sslParameters = sslParameters

                tlsSocket.connect(
                    InetSocketAddress(endpoint.host, endpoint.port),
                    connectTimeoutMs
                )
                tlsSocket.startHandshake()

                val reader = BufferedReader(
                    InputStreamReader(tlsSocket.inputStream, StandardCharsets.UTF_8)
                )
                val welcome = reader.readLine()
                    ?: throw ServerUnavailableException("HELION server closed the connection before sending a welcome line.")

                parseNativeServerWelcome(environment, welcome)
            }
        }
    }
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
