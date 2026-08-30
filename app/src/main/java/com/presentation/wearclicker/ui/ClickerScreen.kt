package com.presentation.wearclicker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.presentation.wearclicker.network.ConnectionState
import com.presentation.wearclicker.ui.theme.BackgroundBlack
import com.presentation.wearclicker.ui.theme.StatusConnected
import com.presentation.wearclicker.ui.theme.StatusConnecting
import com.presentation.wearclicker.ui.theme.StatusError
import com.presentation.wearclicker.ui.theme.TextMuted

/**
 * Full-Screen Presentation Clicker UI for Samsung Galaxy Watch 4.
 *
 * - Solid OLED Black background (#000000)
 * - Single full-screen touch target (Single Tap -> Next, Double Tap -> Prev)
 * - Hardware IMU Wrist Gesture recognition (Outward flick -> Next, Inward flick -> Prev)
 * - Background ambient clock displaying Time, Day, and Date in muted grey (#808080)
 * - Silent navigation without on-screen "Next/Prev" text clutter
 * - 60fps Sinusoidal Expanding Wavy Ripple from touch origin (x, y)
 */
@Composable
fun ClickerScreen(
    viewModel: PresentationViewModel
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val laptopIp by viewModel.laptopIp.collectAsState()
    val gesturesEnabled by viewModel.gesturesEnabled.collectAsState()
    val isConfigOpen by viewModel.isConfigDialogOpen.collectAsState()

    val rippleState = rememberWavyRippleState()

    if (isConfigOpen) {
        IpConfigDialog(
            currentIp = laptopIp,
            gesturesEnabled = gesturesEnabled,
            onSave = { newIp, gesturesOn ->
                viewModel.updateSettings(newIp, gesturesOn)
            },
            onDismiss = { viewModel.closeConfigDialog() }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        // Visual ripple at tap coordinate (x, y)
                        rippleState.spawnRipple(offset, isDoubleTap = false)
                    },
                    onTap = { _ ->
                        // Single Tap: Next slide silently with haptic confirmation
                        viewModel.onSingleTap()
                    },
                    onDoubleTap = { offset ->
                        // Double Tap: Previous slide silently with dual-haptic confirmation
                        rippleState.spawnRipple(offset, isDoubleTap = true)
                        viewModel.onDoubleTap()
                    },
                    onLongPress = {
                        viewModel.openConfigDialog()
                    }
                )
            }
    ) {
        // Ambient Background Clock (Time, Day, Date in subtle muted #808080)
        BackgroundClock(
            modifier = Modifier.align(Alignment.Center)
        )

        // Dynamic Animated Wavy Concentric Rings Canvas
        WavyRippleOverlay(
            state = rippleState,
            modifier = Modifier.fillMaxSize()
        )

        // Top Status Pill (Connection & IP / Quick settings tap)
        TopStatusPill(
            connectionState = connectionState,
            serverIp = laptopIp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            onTap = {
                viewModel.openConfigDialog()
            }
        )

        // Bottom Subtle Interaction Hint
        val hintText = if (gesturesEnabled) "Tap / Flick: Next • 2x: Prev" else "Tap: Next • 2x: Prev"
        Text(
            text = hintText,
            color = TextMuted.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * Sleek Top Pill for OLED displays.
 */
@Composable
private fun TopStatusPill(
    connectionState: ConnectionState,
    serverIp: String,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val (statusColor, statusText) = when (connectionState) {
        is ConnectionState.Connected -> Pair(StatusConnected, "Connected")
        is ConnectionState.Connecting -> Pair(StatusConnecting, "Connecting...")
        is ConnectionState.Disconnected -> Pair(StatusError, "Disconnected")
        is ConnectionState.Error -> Pair(StatusError, "Offline")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E242B))
            .clickable(onClick = onTap)
            .padding(horizontal = 9.dp, vertical = 3.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.5.dp)
                    .background(statusColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (connectionState is ConnectionState.Connected) serverIp else statusText,
                color = Color(0xFFCFD8DC),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
