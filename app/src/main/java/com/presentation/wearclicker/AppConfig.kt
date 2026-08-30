package com.presentation.wearclicker

/**
 * Global Configuration for Wear OS Presentation Clicker.
 * You can set your default laptop IP address here or change it in-app.
 */
object AppConfig {
    /**
     * Default IP address of the laptop running server.py.
     */
    const val DEFAULT_LAPTOP_IP: String = "192.168.1.17"

    /**
     * WebSocket server port matching server.py
     */
    const val SERVER_PORT: Int = 8765

    /**
     * Preference storage keys
     */
    const val PREFS_NAME: String = "wear_clicker_prefs"
    const val KEY_LAPTOP_IP: String = "laptop_ip"
    const val KEY_SERVER_PORT: String = "server_port"

    /**
     * Generates a full WebSocket URL from an IP and Port
     */
    fun buildWebSocketUrl(ip: String, port: Int = SERVER_PORT): String {
        val cleanIp = ip.trim().removePrefix("ws://").removePrefix("http://")
        return "ws://$cleanIp:$port"
    }
}
