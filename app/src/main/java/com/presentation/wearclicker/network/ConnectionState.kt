package com.presentation.wearclicker.network

/**
 * State representing current WebSocket connection to the laptop server.
 */
sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val attempt: Int = 1) : ConnectionState
    data class Connected(val url: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}
