package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.BlockType

/**
 * Authentic Minecraft Bedrock Creative Inventory window:
 * Classical stone-gray container (#C6C6C6) with beveled borders and 3D isometric block icons.
 */
@Composable
fun CreativeInventoryDialog(
    selectedHotbarIndex: Int,
    onSelectBlock: (BlockType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .testTag("creative_inventory_dialog"),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFC6C6C6), // Classic Minecraft stone gray GUI
            border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF373737))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header with Bedrock style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Creative Inventory",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF282828)
                        )
                        Text(
                            text = "Tap a 3D block to place in Slot #${selectedHotbarIndex + 1}",
                            fontSize = 12.sp,
                            color = Color(0xFF4A4A4A)
                        )
                    }

                    // Bedrock styled close button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF373737))
                            .padding(1.dp)
                            .background(Color(0xFF8B8B8B))
                            .clickable(onClick = onDismiss)
                            .testTag("close_inventory_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Inventory",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Inner beveled slot panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF8B8B8B))
                        .border(2.dp, Color(0xFF373737), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 82.dp),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(BlockType.CREATIVE_INVENTORY) { block ->
                            BedrockInventorySlot(
                                block = block,
                                onClick = {
                                    onSelectBlock(block)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single Minecraft Bedrock item slot with inset borders and 3D Isometric block icon
 */
@Composable
private fun BedrockInventorySlot(
    block: BlockType,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF373737)) // Dark outer shadow
            .padding(1.5.dp)
            .background(Color(0xFF8F8F8F)) // Stone slot interior
            .clickable(onClick = onClick)
            .testTag("block_card_${block.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            // Authentic 3D Isometric Voxel Cube
            IsometricBlockIcon(block = block, size = 36.dp)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = block.displayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}
