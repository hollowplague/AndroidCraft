package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Settings dialog for graphics, field of view, sensitivity, inverted look, and world generation.
 */
@Composable
fun GameSettingsDialog(
    fov: Float,
    onFovChange: (Float) -> Unit,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    invertY: Boolean,
    onInvertYToggle: (Boolean) -> Unit,
    fogDistance: Float,
    onFogDistanceChange: (Float) -> Unit,
    timeOfDay: Float,
    onTimeOfDayChange: (Float) -> Unit,
    isTimeFlowing: Boolean,
    onTimeFlowingToggle: (Boolean) -> Unit,
    isFlying: Boolean,
    onFlightToggle: (Boolean) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsToggle: (Boolean) -> Unit,
    onRegenerateWorld: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(480.dp)
                .padding(16.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E222A).copy(alpha = 0.96f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Game Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Flight Mode Toggle
                SettingRow(title = "Creative Flight Mode", subtitle = "Fly freely through the sky") {
                    Switch(
                        checked = isFlying,
                        onCheckedChange = onFlightToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF38A169),
                            checkedTrackColor = Color(0xFF22543D)
                        ),
                        modifier = Modifier.testTag("flight_switch")
                    )
                }

                // Invert Y-Axis Toggle
                SettingRow(title = "Invert Y-Axis", subtitle = "Swipe up looks down (flight stick)") {
                    Switch(
                        checked = invertY,
                        onCheckedChange = onInvertYToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF63B3ED),
                            checkedTrackColor = Color(0xFF2B6CB0)
                        ),
                        modifier = Modifier.testTag("invert_y_switch")
                    )
                }

                // Haptics Toggle
                SettingRow(title = "Haptic Vibration", subtitle = "Tactile feedback on mine & build") {
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = onHapticsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF38A169),
                            checkedTrackColor = Color(0xFF22543D)
                        ),
                        modifier = Modifier.testTag("haptics_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FOV Slider
                Text(
                    text = "Field of View (FOV): ${fov.toInt()}°",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0)
                )
                Slider(
                    value = fov,
                    onValueChange = onFovChange,
                    valueRange = 60f..95f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF63B3ED),
                        activeTrackColor = Color(0xFF3182CE)
                    ),
                    modifier = Modifier.testTag("fov_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Look Sensitivity Slider
                Text(
                    text = "Camera Look Sensitivity: ${(sensitivity * 10).toInt() / 10f}x",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0)
                )
                Slider(
                    value = sensitivity,
                    onValueChange = onSensitivityChange,
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF63B3ED),
                        activeTrackColor = Color(0xFF3182CE)
                    ),
                    modifier = Modifier.testTag("sensitivity_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Render Fog Distance
                Text(
                    text = "Render Fog Distance: ${fogDistance.toInt()}m",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0)
                )
                Slider(
                    value = fogDistance,
                    onValueChange = onFogDistanceChange,
                    valueRange = 35f..75f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF63B3ED),
                        activeTrackColor = Color(0xFF3182CE)
                    ),
                    modifier = Modifier.testTag("fog_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time of Day Presets
                Text(
                    text = "Atmospheric Lighting Preset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "Dawn" to 0.25f,
                        "Noon" to 0.45f,
                        "Sunset" to 0.74f,
                        "Night" to 0.05f
                    )
                    presets.forEach { (label, value) ->
                        FilterChip(
                            selected = kotlin.math.abs(timeOfDay - value) < 0.1f,
                            onClick = { onTimeOfDayChange(value) },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Regenerate World Button
                Button(
                    onClick = {
                        onRegenerateWorld()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("regenerate_world_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53030)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regenerate New World", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF94A3B8))
        }
        control()
    }
}
