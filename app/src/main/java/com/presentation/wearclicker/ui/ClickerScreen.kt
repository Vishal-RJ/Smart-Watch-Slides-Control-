package com.presentation.wearclicker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.presentation.wearclicker.ui.theme.WaveAccent
import com.presentation.wearclicker.ui.theme.WaveLightBlue

/**
 * Full-Screen Gesture-Driven Presentation Clicker UI.
 * - Solid OLED Black background (#000000)
 * - Single full-screen touch target
 * - Single Tap: Next Slide ("NEXT")
 * - Double Tap: Previous Slide ("PREV")
 * - 60fps Sinusoidal Expanding Wavy Ripple from tap origin (x, y)
 */
@Composable
fun ClickerScreen(
    viewModel: PresentationViewModel
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val laptopIp by viewModel.laptopIp.collectAsState()
    val isConfigOpen by viewModel.isConfigDialogOpen.collectAsState()
    val lastAction by viewModel.lastAction.collectAsState()

    val rippleState = rememberWavyRippleState()

    if (isConfigOpen) {
        IpConfigDialog(
            currentIp = laptopIp,
            onSave = { newIp -> viewModel.updateLaptopIp(newIp) },
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
                        // Instant visual ripple at tap coordinate (x, y)
                        rippleState.spawnRipple(offset, isDoubleTap = false)
                    },
                    onTap = { _ ->
                        // Verified Single Tap (Double-tap window passed without 2nd tap)
                        viewModel.onSingleTap()
                    },
                    onDoubleTap = { offset ->
                        // Verified Double Tap -> Prev Slide
                        rippleState.spawnRipple(offset, isDoubleTap = true)
                        viewModel.onDoubleTap()
                    },
                    onLongPress = {
                        viewModel.openConfigDialog()
                    }
                )
            }
    ) {
        // Dynamic Animated Wavy Concentric Rings Canvas
        WavyRippleOverlay(
            state = rippleState,
            modifier = Modifier.fillMaxSize()
        )

        // Center Minimalist HUD & Gesture Indicators
        CenterGestureHUD(
            lastAction = lastAction,
            modifier = Modifier.align(Alignment.Center)
        )

        // Top Status Pill (Connection & IP)
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

        // Bottom Subtle Gesture Hint
        Text(
            text = "Tap: Next • 2x: Prev",
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
 * Center HUD displaying flashing feedback on action (Next / Prev).
 */
@Composable
private fun CenterGestureHUD(
    lastAction: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = lastAction != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val isNext = lastAction == "NEXT"
            val actionText = if (isNext) "NEXT" else "PREV"
            val actionColor = if (isNext) WaveLightBlue else WaveAccent

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(actionColor.copy(alpha = 0.18f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    color = actionColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
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
