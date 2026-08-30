package com.presentation.wearclicker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Text
import com.presentation.wearclicker.ui.theme.BackgroundBlack
import com.presentation.wearclicker.ui.theme.TextLight
import com.presentation.wearclicker.ui.theme.TextMuted
import com.presentation.wearclicker.ui.theme.WaveLightBlue

/**
 * Dark OLED Theme Wear OS Dialog to view and adjust Laptop IP address and Gesture Toggle.
 * Allows interactive adjustment of all 4 octets, quick subnet presets, and wrist gesture toggle.
 */
@Composable
fun IpConfigDialog(
    currentIp: String,
    gesturesEnabled: Boolean,
    onSave: (newIp: String, gesturesEnabled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = currentIp.split(".").mapNotNull { it.toIntOrNull() }
    val initialOctets = if (parts.size == 4) parts else listOf(192, 168, 1, 17)

    var octet0 by remember { mutableIntStateOf(initialOctets[0].coerceIn(0, 255)) }
    var octet1 by remember { mutableIntStateOf(initialOctets[1].coerceIn(0, 255)) }
    var octet2 by remember { mutableIntStateOf(initialOctets[2].coerceIn(0, 255)) }
    var octet3 by remember { mutableIntStateOf(initialOctets[3].coerceIn(0, 255)) }

    var isGesturesOn by remember { mutableStateOf(gesturesEnabled) }

    // 0 = first octet, 1 = second, 2 = third, 3 = fourth (defaulting to last octet for quick adjustment)
    var selectedOctetIndex by remember { mutableIntStateOf(3) }

    fun getSelectedValue(): Int = when (selectedOctetIndex) {
        0 -> octet0
        1 -> octet1
        2 -> octet2
        else -> octet3
    }

    fun modifySelected(delta: Int) {
        val currentVal = getSelectedValue()
        val newVal = (currentVal + delta).coerceIn(0, 255)
        when (selectedOctetIndex) {
            0 -> octet0 = newVal
            1 -> octet1 = newVal
            2 -> octet2 = newVal
            3 -> octet3 = newVal
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Settings & IP Config",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 4-Octet Interactive IP Box Display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val octets = listOf(octet0, octet1, octet2, octet3)
                octets.forEachIndexed { index, value ->
                    val isSelected = selectedOctetIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF00385E) else Color(0xFF1E242B))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) WaveLightBlue else Color(0xFF37474F),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedOctetIndex = index }
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$value",
                            color = if (isSelected) WaveLightBlue else TextLight,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }

                    if (index < 3) {
                        Text(
                            text = ".",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Steppers for selected octet (+ / - buttons)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // -10
                CompactButton(
                    onClick = { modifySelected(-10) },
                    colors = ButtonDefaults.secondaryButtonColors(
                        backgroundColor = Color(0xFF263238),
                        contentColor = TextLight
                    ),
                    modifier = Modifier.size(30.dp)
                ) {
                    Text("-10", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(3.dp))

                // -1
                CompactButton(
                    onClick = { modifySelected(-1) },
                    colors = ButtonDefaults.secondaryButtonColors(
                        backgroundColor = Color(0xFF263238),
                        contentColor = TextLight
                    ),
                    modifier = Modifier.size(30.dp)
                ) {
                    Text("-1", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(3.dp))

                // +1
                CompactButton(
                    onClick = { modifySelected(+1) },
                    colors = ButtonDefaults.secondaryButtonColors(
                        backgroundColor = Color(0xFF263238),
                        contentColor = TextLight
                    ),
                    modifier = Modifier.size(30.dp)
                ) {
                    Text("+1", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(3.dp))

                // +10
                CompactButton(
                    onClick = { modifySelected(+10) },
                    colors = ButtonDefaults.secondaryButtonColors(
                        backgroundColor = Color(0xFF263238),
                        contentColor = TextLight
                    ),
                    modifier = Modifier.size(30.dp)
                ) {
                    Text("+10", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subnet Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PresetChip("192.168.1.x") {
                    octet0 = 192; octet1 = 168; octet2 = 1; selectedOctetIndex = 3
                }
                PresetChip("192.168.0.x") {
                    octet0 = 192; octet1 = 168; octet2 = 0; selectedOctetIndex = 3
                }
                PresetChip("10.0.0.x") {
                    octet0 = 10; octet1 = 0; octet2 = 0; selectedOctetIndex = 3
                }
                PresetChip("127.0.0.1") {
                    octet0 = 127; octet1 = 0; octet2 = 0; octet3 = 1
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hand Gesture Toggle Setting Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A2129))
                    .border(
                        width = 0.8.dp,
                        color = if (isGesturesOn) WaveLightBlue.copy(alpha = 0.5f) else Color(0xFF263238),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { isGesturesOn = !isGesturesOn }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hand Gestures",
                        color = TextLight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGesturesOn) "Wrist flick Next / Prev" else "Disabled (Tap only)",
                        color = if (isGesturesOn) WaveLightBlue else TextMuted,
                        fontSize = 8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGesturesOn) WaveLightBlue else Color(0xFF37474F))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isGesturesOn) "ON" else "OFF",
                        color = if (isGesturesOn) Color(0xFF002B49) else Color(0xFFECEFF1),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Save & Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.secondaryButtonColors(
                        backgroundColor = Color(0xFF1E242B),
                        contentColor = TextMuted
                    ),
                    modifier = Modifier
                        .height(30.dp)
                        .width(54.dp)
                ) {
                    Text("Back", fontSize = 9.sp)
                }

                Button(
                    onClick = { onSave("$octet0.$octet1.$octet2.$octet3", isGesturesOn) },
                    colors = ButtonDefaults.primaryButtonColors(
                        backgroundColor = WaveLightBlue,
                        contentColor = Color(0xFF002B49)
                    ),
                    modifier = Modifier
                        .height(30.dp)
                        .width(62.dp)
                ) {
                    Text("Save", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PresetChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A2129))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF90A4AE),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
