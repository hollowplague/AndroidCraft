package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.game.engine.BlockType

/**
 * Renders an authentic Minecraft-style 3D isometric voxel block icon.
 * Features illuminated Top face (100% brightness), shaded Left face (60% brightness),
 * and diffuse Right face (80% brightness) with characteristic block textures and bevels.
 */
@Composable
fun IsometricBlockIcon(
    block: BlockType,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()

        // Isometric 3D cube geometry centered in canvas
        val centerX = w * 0.5f
        val centerY = h * 0.48f
        val radiusX = w * 0.38f
        val radiusY = h * 0.22f
        val cubeHeight = h * 0.40f

        // 7 key isometric vertices:
        // C: Center top vertex (closest to viewer in top face)
        val pCenter = Offset(centerX, centerY)
        // N: North vertex (far top)
        val pNorth = Offset(centerX, centerY - radiusY)
        // W: West vertex (far left top)
        val pWest = Offset(centerX - radiusX, centerY - radiusY * 0.5f)
        // E: East vertex (far right top)
        val pEast = Offset(centerX + radiusX, centerY - radiusY * 0.5f)
        // SW: South-West vertex (bottom left)
        val pBottomLeft = Offset(centerX - radiusX, centerY - radiusY * 0.5f + cubeHeight)
        // S: South vertex (bottom center)
        val pBottomCenter = Offset(centerX, centerY + cubeHeight)
        // SE: South-East vertex (bottom right)
        val pBottomRight = Offset(centerX + radiusX, centerY - radiusY * 0.5f + cubeHeight)

        val (topColor, leftColor, rightColor) = getBlockIsometricColors(block)

        // 1. LEFT FACE (Shadowed, ~60% brightness)
        val leftPath = Path().apply {
            moveTo(pCenter.x, pCenter.y)
            lineTo(pWest.x, pWest.y)
            lineTo(pBottomLeft.x, pBottomLeft.y)
            lineTo(pBottomCenter.x, pBottomCenter.y)
            close()
        }
        drawPath(leftPath, color = leftColor)
        drawFaceDetails(block, isSide = true, isLeft = true, pCenter, pWest, pBottomLeft, pBottomCenter)

        // 2. RIGHT FACE (Diffuse, ~80% brightness)
        val rightPath = Path().apply {
            moveTo(pCenter.x, pCenter.y)
            lineTo(pEast.x, pEast.y)
            lineTo(pBottomRight.x, pBottomRight.y)
            lineTo(pBottomCenter.x, pBottomCenter.y)
            close()
        }
        drawPath(rightPath, color = rightColor)
        drawFaceDetails(block, isSide = true, isLeft = false, pCenter, pEast, pBottomRight, pBottomCenter)

        // 3. TOP FACE (Illuminated, 100% brightness)
        val topPath = Path().apply {
            moveTo(pNorth.x, pNorth.y)
            lineTo(pEast.x, pEast.y)
            lineTo(pCenter.x, pCenter.y)
            lineTo(pWest.x, pWest.y)
            close()
        }
        drawPath(topPath, color = topColor)
        drawTopFaceDetails(block, pNorth, pEast, pCenter, pWest)

        // 4. CRISP SILHOUETTE OUTLINE
        val outlineColor = Color(0xFF181818).copy(alpha = 0.85f)
        val strokeW = 1.5f

        // Outer contour
        drawLine(outlineColor, pNorth, pEast, strokeW)
        drawLine(outlineColor, pEast, pBottomRight, strokeW)
        drawLine(outlineColor, pBottomRight, pBottomCenter, strokeW)
        drawLine(outlineColor, pBottomCenter, pBottomLeft, strokeW)
        drawLine(outlineColor, pBottomLeft, pWest, strokeW)
        drawLine(outlineColor, pWest, pNorth, strokeW)

        // Inner 3 central edges meeting at pCenter
        val innerEdgeColor = Color(0xFF000000).copy(alpha = 0.35f)
        drawLine(innerEdgeColor, pCenter, pNorth, strokeW)
        drawLine(innerEdgeColor, pCenter, pBottomCenter, strokeW)
        drawLine(innerEdgeColor, pCenter, pWest, strokeW)
        drawLine(innerEdgeColor, pCenter, pEast, strokeW)
    }
}

/**
 * Face detail decorations for authentic block identification
 */
private fun DrawScope.drawFaceDetails(
    block: BlockType,
    isSide: Boolean,
    isLeft: Boolean,
    pTopCenter: Offset,
    pTopOuter: Offset,
    pBottomOuter: Offset,
    pBottomCenter: Offset
) {
    when (block) {
        BlockType.GRASS -> {
            // Grass green fringe hanging over dirt side
            val grassPath = Path().apply {
                moveTo(pTopCenter.x, pTopCenter.y)
                lineTo(pTopOuter.x, pTopOuter.y)
                lineTo(
                    pTopOuter.x,
                    pTopOuter.y + (pBottomOuter.y - pTopOuter.y) * 0.32f
                )
                lineTo(
                    pTopCenter.x + (pTopOuter.x - pTopCenter.x) * 0.5f,
                    pTopCenter.y + (pBottomCenter.y - pTopCenter.y) * 0.42f
                )
                lineTo(
                    pTopCenter.x,
                    pTopCenter.y + (pBottomCenter.y - pTopCenter.y) * 0.28f
                )
                close()
            }
            val grassSideColor = if (isLeft) Color(0xFF4C7C23) else Color(0xFF5B8C33)
            drawPath(grassPath, grassSideColor)
        }
        BlockType.OAK_WOOD -> {
            // Vertical bark ridges
            val barkLine = if (isLeft) Color(0xFF3B2814) else Color(0xFF4A341C)
            val midTop = Offset(
                pTopCenter.x + (pTopOuter.x - pTopCenter.x) * 0.5f,
                pTopCenter.y + (pTopOuter.y - pTopCenter.y) * 0.5f
            )
            val midBottom = Offset(
                pBottomCenter.x + (pBottomOuter.x - pBottomCenter.x) * 0.5f,
                pBottomCenter.y + (pBottomOuter.y - pBottomCenter.y) * 0.5f
            )
            drawLine(barkLine, midTop, midBottom, 1.5f)
        }
        BlockType.WOOD_PLANKS -> {
            // Horizontal wood plank seam
            val seamColor = Color(0xFF4C371D).copy(alpha = 0.6f)
            val midInner = Offset(
                pTopCenter.x + (pBottomCenter.x - pTopCenter.x) * 0.5f,
                pTopCenter.y + (pBottomCenter.y - pTopCenter.y) * 0.5f
            )
            val midOuter = Offset(
                pTopOuter.x + (pBottomOuter.x - pTopOuter.x) * 0.5f,
                pTopOuter.y + (pBottomOuter.y - pTopOuter.y) * 0.5f
            )
            drawLine(seamColor, midInner, midOuter, 1.5f)
        }
        BlockType.BRICK -> {
            // Mortar line
            val mortarColor = Color(0xFFD8CFBC).copy(alpha = 0.65f)
            val midInner = Offset(
                pTopCenter.x + (pBottomCenter.x - pTopCenter.x) * 0.5f,
                pTopCenter.y + (pBottomCenter.y - pTopCenter.y) * 0.5f
            )
            val midOuter = Offset(
                pTopOuter.x + (pBottomOuter.x - pTopOuter.x) * 0.5f,
                pTopOuter.y + (pBottomOuter.y - pTopOuter.y) * 0.5f
            )
            drawLine(mortarColor, midInner, midOuter, 1.2f)
        }
        BlockType.GLASS -> {
            // Diagonal glass glint
            val glintColor = Color.White.copy(alpha = 0.7f)
            val g1 = Offset(
                pTopCenter.x + (pTopOuter.x - pTopCenter.x) * 0.3f,
                pTopCenter.y + (pTopOuter.y - pTopCenter.y) * 0.3f + 3f
            )
            val g2 = Offset(
                pTopCenter.x + (pBottomCenter.x - pTopCenter.x) * 0.4f,
                pTopCenter.y + (pBottomCenter.y - pTopCenter.y) * 0.4f + 3f
            )
            drawLine(glintColor, g1, g2, 1.5f)
        }
        else -> {}
    }
}

private fun DrawScope.drawTopFaceDetails(
    block: BlockType,
    pNorth: Offset, pEast: Offset, pCenter: Offset, pWest: Offset
) {
    if (block == BlockType.OAK_WOOD) {
        // Concentric tree rings on log top
        val midX = (pNorth.x + pCenter.x) * 0.5f
        val midY = (pNorth.y + pCenter.y) * 0.5f
        drawCircle(Color(0xFF856637), radius = 3.5f, center = Offset(midX, midY))
        drawCircle(Color(0xFF7D5B32), radius = 1.5f, center = Offset(midX, midY))
    }
}

private data class IsometricColors(val top: Color, val left: Color, val right: Color)

private fun getBlockIsometricColors(block: BlockType): IsometricColors {
    return when (block) {
        BlockType.GRASS -> IsometricColors(
            top = Color(0xFF62A431),   // Lush green top
            left = Color(0xFF6B4B31),  // Dirt left
            right = Color(0xFF866043)  // Dirt right
        )
        BlockType.DIRT -> IsometricColors(
            top = Color(0xFF966D4C),
            left = Color(0xFF6B4B31),
            right = Color(0xFF866043)
        )
        BlockType.STONE -> IsometricColors(
            top = Color(0xFF8C8C8C),
            left = Color(0xFF5A5A5A),
            right = Color(0xFF737373)
        )
        BlockType.COBBLESTONE -> IsometricColors(
            top = Color(0xFF888888),
            left = Color(0xFF525252),
            right = Color(0xFF6E6E6E)
        )
        BlockType.OAK_WOOD -> IsometricColors(
            top = Color(0xFFA68550),   // Tree rings top
            left = Color(0xFF4A341C),  // Dark bark
            right = Color(0xFF684B29)  // Medium bark
        )
        BlockType.OAK_LEAVES -> IsometricColors(
            top = Color(0xFF4A8022),
            left = Color(0xFF2C5012),
            right = Color(0xFF386618)
        )
        BlockType.WOOD_PLANKS -> IsometricColors(
            top = Color(0xFFB58E59),
            left = Color(0xFF7D5B32),
            right = Color(0xFFA17C4D)
        )
        BlockType.BRICK -> IsometricColors(
            top = Color(0xFFB05144),
            left = Color(0xFF782E24),
            right = Color(0xFF9C4336)
        )
        BlockType.SAND -> IsometricColors(
            top = Color(0xFFE5DDAF),
            left = Color(0xFFBFB078),
            right = Color(0xFFDBD3A0)
        )
        BlockType.GLASS -> IsometricColors(
            top = Color(0x66D8EEFF),
            left = Color(0x77A8D4F8),
            right = Color(0x66B8DEFB)
        )
        BlockType.WATER -> IsometricColors(
            top = Color(0xB03C6FF0),
            left = Color(0xC01C44C2),
            right = Color(0xB52E5CE6)
        )
        BlockType.GLOWSTONE -> IsometricColors(
            top = Color(0xFFFFF099),
            left = Color(0xFFD4A030),
            right = Color(0xFFF0BC40)
        )
        BlockType.BEDROCK -> IsometricColors(
            top = Color(0xFF484848),
            left = Color(0xFF202020),
            right = Color(0xFF303030)
        )
        BlockType.BOOKSHELF -> IsometricColors(
            top = Color(0xFFB58E59),   // Planks top
            left = Color(0xFF7A4A28),  // Shelves left
            right = Color(0xFF8A5A36)  // Shelves right
        )
        else -> IsometricColors(Color.LightGray, Color.Gray, Color.DarkGray)
    }
}
