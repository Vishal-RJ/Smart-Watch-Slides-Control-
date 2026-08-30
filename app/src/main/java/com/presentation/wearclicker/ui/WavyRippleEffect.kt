package com.presentation.wearclicker.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.presentation.wearclicker.ui.theme.WaveAccent
import com.presentation.wearclicker.ui.theme.WaveCyan
import com.presentation.wearclicker.ui.theme.WaveLightBlue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a single expanding wavy ripple event.
 */
data class RippleInstance(
    val id: Long,
    val center: Offset,
    val isDoubleTap: Boolean = false
)

/**
 * State controller for managing active wavy ripples.
 */
class WavyRippleState {
    val activeRipples = mutableStateListOf<RippleInstance>()
    private var counter = 0L

    fun spawnRipple(center: Offset, isDoubleTap: Boolean = false) {
        val id = ++counter
        activeRipples.add(RippleInstance(id, center, isDoubleTap))
    }

    fun removeRipple(id: Long) {
        activeRipples.removeAll { it.id == id }
    }
}

@Composable
fun rememberWavyRippleState(): WavyRippleState {
    return remember { WavyRippleState() }
}

/**
 * 60fps Custom Canvas rendering concentric sinusoidal/wavy expanding rings.
 */
@Composable
fun WavyRippleOverlay(
    state: WavyRippleState,
    modifier: Modifier = Modifier
) {
    for (ripple in state.activeRipples) {
        SingleWavyRipple(
            ripple = ripple,
            onFinished = { state.removeRipple(ripple.id) },
            modifier = modifier
        )
    }
}

@Composable
private fun SingleWavyRipple(
    ripple: RippleInstance,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(ripple.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (ripple.isDoubleTap) 850 else 700,
                easing = LinearEasing
            )
        )
        onFinished()
    }

    val p = progress.value
    val baseColor = if (ripple.isDoubleTap) WaveAccent else WaveLightBlue
    val secondaryColor = if (ripple.isDoubleTap) WaveLightBlue else WaveCyan

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxRadius = size.maxDimension * 0.70f
        val numRings = if (ripple.isDoubleTap) 4 else 3
        val segments = 80 // Number of vertices around the circumference

        // Center flash dot at early stage of tap
        if (p < 0.25f) {
            val centerAlpha = (1f - (p / 0.25f)) * 0.9f
            val centerRadius = (p / 0.25f) * 16.dp.toPx()
            drawCircle(
                color = baseColor.copy(alpha = centerAlpha),
                radius = centerRadius,
                center = ripple.center
            )
        }

        // Draw multiple expanding concentric wavy rings
        for (ringIndex in 0 until numRings) {
            val ringOffset = ringIndex * 0.12f
            if (p < ringOffset) continue

            // Normalized ring progress: [0..1]
            val ringProgress = ((p - ringOffset) / (1f - ringOffset)).coerceIn(0f, 1f)

            // Easing: fast expansion initially, slowing gently as it travels outward
            val easeQuad = 1f - (1f - ringProgress) * (1f - ringProgress)
            val ringBaseRadius = easeQuad * maxRadius

            if (ringBaseRadius <= 2f) continue

            // Opacity: smoothly decreases to 0 as it expands outward
            val ringFade = (1f - ringProgress)
            val ringAlpha = (ringFade * ringFade * 0.85f * (1f - ringIndex * 0.15f)).coerceIn(0f, 1f)

            // Line thickness: gently tapers from 3.2dp down to 0.6dp
            val strokeWidth = (3.2.dp.toPx() * (1f - ringProgress * 0.75f)).coerceAtLeast(0.6.dp.toPx())

            // Sinusoidal wave distortion parameters
            val lobes = 8 + ringIndex // 8, 9, 10 lobes around circumference
            val maxAmplitude = (5.5.dp.toPx() * sin(ringProgress * PI.toFloat()) * (1f - ringProgress * 0.3f))
            val phaseShift = (ringProgress * 5.0f * PI.toFloat()) + (ringIndex * 0.8f)

            val path = Path()
            var isFirst = true

            val angleStep = (2.0 * Math.PI) / segments

            for (i in 0..segments) {
                val theta = i * angleStep
                // Primary sinusoidal distortion + secondary harmonic for organic fluid propagation
                val waveDistortion = (sin(lobes * theta - phaseShift) * maxAmplitude) +
                        (cos(lobes * 2.0 * theta + phaseShift * 0.5) * (maxAmplitude * 0.25))

                val r = (ringBaseRadius + waveDistortion).coerceAtLeast(0.0)
                val x = (ripple.center.x + r * cos(theta)).toFloat()
                val y = (ripple.center.y + r * sin(theta)).toFloat()

                if (isFirst) {
                    path.moveTo(x, y)
                    isFirst = false
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()

            val ringColor = if (ringIndex % 2 == 0) baseColor else secondaryColor

            drawPath(
                path = path,
                color = ringColor.copy(alpha = ringAlpha),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
