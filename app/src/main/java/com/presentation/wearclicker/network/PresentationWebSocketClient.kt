package com.presentation.wearclicker.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Robust OkHttp-based WebSocket client for communicating with the laptop presentation receiver.
 * Includes automatic reconnection with exponential backoff and connection state tracking.
 */
class PresentationWebSocketClient(
    private var serverUrl: String
) {
    private val tag = "PresentationWSClient"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .pingInterval(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectJob: Job? = null

    private val isManuallyClosed = AtomicBoolean(false)
    private var reconnectAttempts = 0

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /**
     * Connect to the current serverUrl.
     */
    fun connect() {
        isManuallyClosed.set(false)
        reconnectAttempts = 0
        initiateConnection()
    }

    /**
     * Update target server URL and reconnect immediately.
     */
    fun updateServerUrl(newUrl: String) {
        if (serverUrl == newUrl && _connectionState.value is ConnectionState.Connected) {
            return
        }
        serverUrl = newUrl
        disconnect()
        connect()
    }

    /**
     * Send presentation command ("NEXT" or "PREV").
     */
    fun sendCommand(command: String): Boolean {
        val currentWs = webSocket
        if (currentWs != null && _connectionState.value is ConnectionState.Connected) {
            val sent = currentWs.send(command)
            Log.d(tag, "Sent command: $command (success=$sent)")
            return sent
        } else {
            Log.w(tag, "Failed to send $command - not connected")
            // Try reconnecting in background if not already connected
            scheduleReconnect(immediate = true)
            return false
        }
    }

    /**
     * Disconnect cleanly and cancel auto-reconnect.
     */
    fun disconnect() {
        isManuallyClosed.set(true)
        reconnectJob?.cancel()
        reconnectJob = null

        try {
            webSocket?.close(1000, "App closed")
        } catch (e: Exception) {
            Log.w(tag, "Error closing WebSocket: ${e.message}")
        } finally {
            webSocket = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private fun initiateConnection() {
        reconnectJob?.cancel()

        if (serverUrl.isBlank()) {
            _connectionState.value = ConnectionState.Error("Server URL is empty")
            return
        }

        _connectionState.value = ConnectionState.Connecting(attempt = reconnectAttempts + 1)
        Log.i(tag, "Connecting to $serverUrl (attempt ${reconnectAttempts + 1})...")

        try {
            val request = Request.Builder()
                .url(serverUrl)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.i(tag, "Connected to $serverUrl")
                    reconnectAttempts = 0
                    _connectionState.value = ConnectionState.Connected(serverUrl)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(tag, "Received message: $text")
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.i(tag, "Closing connection: $code / $reason")
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.i(tag, "Connection closed: $code / $reason")
                    _connectionState.value = ConnectionState.Disconnected
                    if (!isManuallyClosed.get()) {
                        scheduleReconnect()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    val errMsg = t.message ?: "Unknown connection error"
                    Log.e(tag, "WebSocket failure: $errMsg")
                    _connectionState.value = ConnectionState.Error(errMsg)
                    if (!isManuallyClosed.get()) {
                        scheduleReconnect()
                    }
                }
            })
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Failed to initialize connection"
            Log.e(tag, "Connection creation failed: $errorMsg")
            _connectionState.value = ConnectionState.Error(errorMsg)
            if (!isManuallyClosed.get()) {
                scheduleReconnect()
            }
        }
    }

    private fun scheduleReconnect(immediate: Boolean = false) {
        if (isManuallyClosed.get()) return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            if (!immediate) {
                reconnectAttempts++
                // Exponential backoff: 1s, 2s, 4s, capped at 10s
                val delayMs = when {
                    reconnectAttempts <= 1 -> 1000L
                    reconnectAttempts == 2 -> 2000L
                    reconnectAttempts == 3 -> 4000L
                    else -> 8000L
                }
                Log.d(tag, "Scheduling reconnect attempt $reconnectAttempts in ${delayMs}ms...")
                delay(delayMs)
            }
            if (!isManuallyClosed.get()) {
                initiateConnection()
            }
        }
    }
}
