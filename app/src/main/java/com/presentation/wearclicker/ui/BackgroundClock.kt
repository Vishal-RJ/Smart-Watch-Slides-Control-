package com.presentation.wearclicker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ambient background clock for the main clicker screen.
 * Displays Time, Day of the week, and Date in subtle muted grey (#808080)
 * at a low alpha so it stays readable while idle without interfering with
 * touch targets or active ripple visuals.
 */
@Composable
fun BackgroundClock(
    modifier: Modifier = Modifier
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Periodically update the time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val currentDate = remember(currentTimeMillis / 1000) { Date(currentTimeMillis) }

    val timeFormat = remember { SimpleDateFormat("hh:mm", Locale.getDefault()) }
    val amPmFormat = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val dayDateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }

    val timeString = timeFormat.format(currentDate)
    val amPmString = amPmFormat.format(currentDate).uppercase(Locale.getDefault())
    val dayDateString = dayDateFormat.format(currentDate)

    val clockColor = Color(0xFF808080).copy(alpha = 0.38f)
    val dateColor = Color(0xFF808080).copy(alpha = 0.30f)

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Time Display (e.g. "10:42 PM")
        Text(
            text = "$timeString $amPmString",
            color = clockColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Day of Week and Date (e.g. "Sunday, Aug 30")
        Text(
            text = dayDateString,
            color = dateColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            letterSpacing = 0.2.sp
        )
    }
}
