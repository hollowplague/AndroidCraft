package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.BlockType
import com.example.game.engine.GameRenderer
import com.example.game.engine.Player
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Clean, authentic Minecraft mobile HUD:
 * - Virtual joystick on the left.
 * - Jump button on the bottom right.
 * - Side-by-side Break and Place buttons directly above Jump.
 * - Smooth camera drag on right half of screen.
 * - Hotbar and top status bar.
 */
@Composable
fun GameControlsOverlay(
    player: Player,
    renderer: GameRenderer,
    sensitivity: Float,
    invertY: Boolean = false,
    hotbarSlots: List<BlockType>,
    selectedSlotIndex: Int,
    onSelectSlot: (Int) -> Unit,
    onOpenInventory: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTime: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("game_controls_overlay")
    ) {
        // ---------------- 1. RIGHT-SIDE TOUCH DRAG LOOK AREA ----------------
        // Captures swipe gestures on the right 55% of the screen.
        // Swiping up (negative dragAmount.y) tilts the camera up towards the sky by default.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 220.dp) // Leave left area clear for joystick
                .pointerInput(sensitivity, invertY) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaX = dragAmount.x * 0.12f * sensitivity
                        val pitchSign = if (invertY) 1f else -1f
                        val deltaPitch = dragAmount.y * 0.12f * sensitivity * pitchSign
                        player.rotate(deltaX, deltaPitch)
                    }
                }
        )

        // ---------------- 2. TOP STATUS BAR ----------------
        TopStatusBar(
            player = player,
            renderer = renderer,
            onOpenSettings = onOpenSettings,
            onToggleTime = onToggleTime,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ---------------- 3. CENTER CROSSHAIR RETICLE ----------------
        CenterCrosshair(modifier = Modifier.align(Alignment.Center))

        // ---------------- 4. LEFT VIRTUAL JOYSTICK ----------------
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
        ) {
            VirtualJoystick(
                onMove = { forward, right ->
                    player.inputForward = forward
                    player.inputRight = right
                }
            )
        }

        // ---------------- 5. RIGHT BUTTONS: BREAK & PLACE ABOVE JUMP ----------------
        RightActionCluster(
            renderer = renderer,
            player = player,
            selectedBlock = hotbarSlots.getOrElse(selectedSlotIndex) { BlockType.GRASS },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        )

        // ---------------- 6. BOTTOM HOTBAR ----------------
        HotbarRow(
            slots = hotbarSlots,
            selectedIndex = selectedSlotIndex,
            onSelectSlot = onSelectSlot,
            onOpenInventory = onOpenInventory,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun TopStatusBar(
    player: Player,
    renderer: GameRenderer,
    onOpenSettings: () -> Unit,
    onToggleTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coords & FPS Chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.55f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "XYZ: ${"%.1f".format(player.posX)}, ${"%.1f".format(player.posY)}, ${"%.1f".format(player.posZ)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF68D391)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${renderer.fps} FPS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF63B3ED)
                )

                renderer.currentRaycastHit?.let { hit ->
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Target: ${hit.block.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        // Action Icons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onToggleTime,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .testTag("toggle_time_button")
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Toggle Time of Day",
                    tint = Color(0xFFECC94B),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .testTag("open_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Game Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CenterCrosshair(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 2.dp)
                .background(Color.White.copy(alpha = 0.85f))
        )
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 16.dp)
                .background(Color.White.copy(alpha = 0.85f))
        )
    }
}

@Composable
private fun VirtualJoystick(
    onMove: (forward: Float, right: Float) -> Unit
) {
    val maxRadiusPx = 110f
    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size(110.dp)
            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val dist = sqrt(dx * dx + dy * dy)
                        val clampedDist = dist.coerceAtMost(maxRadiusPx)
                        val angle = kotlin.math.atan2(dy, dx)
                        knobOffsetX = (kotlin.math.cos(angle) * clampedDist)
                        knobOffsetY = (kotlin.math.sin(angle) * clampedDist)

                        onMove(-knobOffsetY / maxRadiusPx, knobOffsetX / maxRadiusPx)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = knobOffsetX + dragAmount.x
                        val newY = knobOffsetY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)
                        if (dist <= maxRadiusPx) {
                            knobOffsetX = newX
                            knobOffsetY = newY
                        } else {
                            val factor = maxRadiusPx / dist
                            knobOffsetX = newX * factor
                            knobOffsetY = newY * factor
                        }

                        onMove(-knobOffsetY / maxRadiusPx, knobOffsetX / maxRadiusPx)
                    },
                    onDragEnd = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffsetX.roundToInt(), knobOffsetY.roundToInt()) }
                .size(46.dp)
                .background(Color.White.copy(alpha = 0.75f), CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

/**
 * Right-side action controls with authentic Minecraft stone-beveled buttons:
 * - BREAK and PLACE side-by-side on top.
 * - JUMP button directly below them.
 */
@Composable
private fun RightActionCluster(
    renderer: GameRenderer,
    player: Player,
    selectedBlock: BlockType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row of side-by-side BREAK and PLACE buttons directly above JUMP
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BREAK (Mine) Button - Minecraft styled red stone button
            BedrockStoneButton(
                modifier = Modifier
                    .size(54.dp)
                    .testTag("action_break"),
                baseColor = Color(0xFFB53228),
                highlightColor = Color(0xFFE86055),
                shadowColor = Color(0xFF5A1510),
                onClick = { renderer.pendingMine = true }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Break Block",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "BREAK",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // PLACE (Build) Button - Minecraft styled green stone button
            BedrockStoneButton(
                modifier = Modifier
                    .size(54.dp)
                    .testTag("action_place"),
                baseColor = Color(0xFF2E8540),
                highlightColor = Color(0xFF56B86C),
                shadowColor = Color(0xFF144D20),
                onClick = { renderer.pendingPlaceType = selectedBlock }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Place Block",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PLACE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // JUMP Button directly underneath BREAK and PLACE
        BedrockStoneButton(
            modifier = Modifier
                .width(116.dp)
                .height(48.dp)
                .testTag("action_jump"),
            baseColor = Color(0xFF6B7280),
            highlightColor = Color(0xFFA0AEC0),
            shadowColor = Color(0xFF374151),
            onClick = { player.inputJump = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Jump",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "JUMP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Authentic Minecraft beveled stone button
 */
@Composable
private fun BedrockStoneButton(
    modifier: Modifier = Modifier,
    baseColor: Color,
    highlightColor: Color,
    shadowColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(shadowColor)
            .clickable(onClick = onClick)
            .padding(top = 1.dp, start = 1.dp, end = 2.dp, bottom = 3.dp)
            .background(highlightColor, RoundedCornerShape(3.dp))
            .padding(top = 2.dp, start = 2.dp, end = 1.dp, bottom = 1.dp)
            .background(baseColor, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Authentic Minecraft Bedrock Mobile Hotbar:
 * Stone gray bar with 9 inset beveled slots and raised white active selector box.
 */
@Composable
private fun HotbarRow(
    slots: List<BlockType>,
    selectedIndex: Int,
    onSelectSlot: (Int) -> Unit,
    onOpenInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("hotbar_container"),
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF8B8B8B),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF373737))
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            slots.forEachIndexed { index, block ->
                val isSelected = index == selectedIndex
                // Each Bedrock slot has dark top/left inner shadow and light bottom/right highlight
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) Color(0xFFFFFFFF) else Color(0xFF373737))
                        .padding(if (isSelected) 2.5.dp else 1.5.dp)
                        .background(Color(0xFF8F8F8F))
                        .clickable { onSelectSlot(index) }
                        .testTag("hotbar_slot_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    // Authentic 3D Isometric Voxel Block
                    IsometricBlockIcon(block = block, size = 26.dp)
                }
            }

            // Inventory drawer toggle button styled as Bedrock slot
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF373737))
                    .padding(1.5.dp)
                    .background(Color(0xFF6B7280))
                    .clickable(onClick = onOpenInventory)
                    .testTag("inventory_drawer_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Open Inventory",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
