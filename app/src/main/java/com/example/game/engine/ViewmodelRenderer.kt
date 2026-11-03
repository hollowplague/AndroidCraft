package com.example.game.engine

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Renders the authentic first-person 3D player arm and held voxel block
 * in the lower-right viewport with walking bobbing and punch/place swing animations.
 */
class ViewmodelRenderer {

    private val viewmodelProj = FloatArray(16)
    private val viewmodelMVP = FloatArray(16)
    private val viewmodelModel = FloatArray(16)
    private val tempMat = FloatArray(16)

    // Animation state
    private var walkBobTimer = 0f
    private var swingProgress = 1.0f // 1.0 = idle, 0.0..1.0 = swinging
    private val SWING_DURATION = 0.22f // seconds

    // Vertex buffers
    private var blockBuffer: FloatBuffer? = null
    private var armBuffer: FloatBuffer? = null
    private var lastHeldBlock: BlockType? = null

    fun triggerSwing() {
        swingProgress = 0.0f
    }

    fun update(deltaSec: Float, isMoving: Boolean) {
        // Update walking bobbing
        if (isMoving) {
            walkBobTimer += deltaSec * 8.5f
        } else {
            walkBobTimer += deltaSec * 1.5f // Gentle breathing idle
        }

        // Update swing animation
        if (swingProgress < 1.0f) {
            swingProgress = (swingProgress + deltaSec / SWING_DURATION).coerceAtMost(1.0f)
        }
    }

    /**
     * Renders the 3D player arm and held block in a dedicated viewmodel pass.
     */
    fun render(
        shader: VoxelShader,
        textureId: Int,
        heldBlock: BlockType,
        aspect: Float,
        sunDir: FloatArray,
        sunColor: FloatArray,
        ambColor: FloatArray
    ) {
        if (heldBlock == BlockType.AIR) return

        // 1. Clear depth buffer so held item never clips into nearby terrain/walls
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        // 2. Setup dedicated first-person camera projection (fixed 52 deg FOV)
        Matrix.perspectiveM(viewmodelProj, 0, 52.0f, aspect, 0.05f, 20.0f)

        // 3. Compute Viewmodel Model-View Matrix
        Matrix.setIdentityM(viewmodelModel, 0)

        // Walking bobbing offset
        val bobX = sin(walkBobTimer) * 0.014f
        val bobY = abs(sin(walkBobTimer * 2f)) * 0.012f

        // Swing animation calculations
        val swingSin = if (swingProgress < 1.0f) sin(swingProgress * PI.toFloat()) else 0.0f
        val swingRotX = swingSin * 35.0f
        val swingRotY = -swingSin * 40.0f
        val swingTransY = -swingSin * 0.12f
        val swingTransZ = swingSin * 0.08f

        // Base position in lower-right of screen
        val posX = 0.36f + bobX
        val posY = -0.32f + bobY + swingTransY
        val posZ = -0.65f + swingTransZ

        Matrix.translateM(viewmodelModel, 0, posX, posY, posZ)

        // Isometric held orientation (~30 deg yaw, ~15 deg pitch, ~-12 deg roll)
        Matrix.rotateM(viewmodelModel, 0, 15f + swingRotX, 1f, 0f, 0f)
        Matrix.rotateM(viewmodelModel, 0, -32f + swingRotY, 0f, 1f, 0f)
        Matrix.rotateM(viewmodelModel, 0, -10f, 0f, 0f, 1f)

        // Compute MVP
        Matrix.multiplyMM(viewmodelMVP, 0, viewmodelProj, 0, viewmodelModel, 0)

        // 4. Build/Update held block buffer if changed
        if (heldBlock != lastHeldBlock || blockBuffer == null) {
            lastHeldBlock = heldBlock
            blockBuffer = buildHeldBlockMesh(heldBlock)
        }

        // 5. Build arm buffer once
        if (armBuffer == null) {
            armBuffer = buildArmMesh()
        }

        // 6. Draw with VoxelShader
        GLES20.glUseProgram(shader.programId)
        GLES20.glUniformMatrix4fv(shader.uMVPMatrixLoc, 1, false, viewmodelMVP, 0)
        GLES20.glUniform3f(shader.uCameraPosLoc, 0f, 0f, 0f)
        GLES20.glUniform3f(shader.uSunDirLoc, 0.5f, 0.8f, 0.3f)
        GLES20.glUniform3f(shader.uSunColorLoc, 1.0f, 0.95f, 0.90f)
        GLES20.glUniform3f(shader.uAmbientColorLoc, 0.65f, 0.65f, 0.68f)
        GLES20.glUniform3f(shader.uFogColorLoc, 0f, 0f, 0f)
        GLES20.glUniform1f(shader.uFogStartLoc, 100f) // No fog on viewmodel
        GLES20.glUniform1f(shader.uFogEndLoc, 200f)
        GLES20.glUniform1f(shader.uAlphaMultiplierLoc, 1.0f)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(shader.uTextureLoc, 0)

        // Draw held block
        blockBuffer?.let { buf ->
            drawBuffer(shader, buf, 36) // 6 faces * 2 tris * 3 verts = 36 verts
        }

        // Draw arm underneath holding the block
        armBuffer?.let { buf ->
            drawBuffer(shader, buf, 36)
        }
    }

    private fun drawBuffer(shader: VoxelShader, buffer: FloatBuffer, count: Int) {
        val stride = Chunk.FLOATS_PER_VERTEX * 4

        buffer.position(0)
        GLES20.glEnableVertexAttribArray(shader.aPositionLoc)
        GLES20.glVertexAttribPointer(shader.aPositionLoc, 3, GLES20.GL_FLOAT, false, stride, buffer)

        buffer.position(3)
        GLES20.glEnableVertexAttribArray(shader.aNormalLoc)
        GLES20.glVertexAttribPointer(shader.aNormalLoc, 3, GLES20.GL_FLOAT, false, stride, buffer)

        buffer.position(6)
        GLES20.glEnableVertexAttribArray(shader.aTexCoordLoc)
        GLES20.glVertexAttribPointer(shader.aTexCoordLoc, 2, GLES20.GL_FLOAT, false, stride, buffer)

        buffer.position(8)
        GLES20.glEnableVertexAttribArray(shader.aAOLoc)
        GLES20.glVertexAttribPointer(shader.aAOLoc, 1, GLES20.GL_FLOAT, false, stride, buffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count)

        GLES20.glDisableVertexAttribArray(shader.aPositionLoc)
        GLES20.glDisableVertexAttribArray(shader.aNormalLoc)
        GLES20.glDisableVertexAttribArray(shader.aTexCoordLoc)
        GLES20.glDisableVertexAttribArray(shader.aAOLoc)
    }

    /**
     * Builds a 3D miniature cube mesh representing the held item.
     */
    private fun buildHeldBlockMesh(block: BlockType): FloatBuffer {
        val list = ArrayList<Float>(36 * Chunk.FLOATS_PER_VERTEX)
        val s = 0.12f // Half-extent = 0.24 cube size
        val cx = 0.0f
        val cy = 0.05f
        val cz = 0.0f

        val topUV = TextureAtlas.getTileUV(block.topTex)
        val bottomUV = TextureAtlas.getTileUV(block.bottomTex)
        val sideUV = TextureAtlas.getTileUV(block.sideTex)

        // Top (+Y)
        addQuad(list,
            cx - s, cy + s, cz - s,
            cx + s, cy + s, cz - s,
            cx + s, cy + s, cz + s,
            cx - s, cy + s, cz + s,
            0f, 1f, 0f, topUV, 1.0f
        )
        // Bottom (-Y)
        addQuad(list,
            cx - s, cy - s, cz + s,
            cx + s, cy - s, cz + s,
            cx + s, cy - s, cz - s,
            cx - s, cy - s, cz - s,
            0f, -1f, 0f, bottomUV, 0.5f
        )
        // North (+Z)
        addQuad(list,
            cx + s, cy + s, cz + s,
            cx - s, cy + s, cz + s,
            cx - s, cy - s, cz + s,
            cx + s, cy - s, cz + s,
            0f, 0f, 1f, sideUV, 0.8f
        )
        // South (-Z)
        addQuad(list,
            cx - s, cy + s, cz - s,
            cx + s, cy + s, cz - s,
            cx + s, cy - s, cz - s,
            cx - s, cy - s, cz - s,
            0f, 0f, -1f, sideUV, 0.8f
        )
        // East (+X)
        addQuad(list,
            cx + s, cy + s, cz + s,
            cx + s, cy + s, cz - s,
            cx + s, cy - s, cz - s,
            cx + s, cy - s, cz + s,
            1f, 0f, 0f, sideUV, 0.65f
        )
        // West (-X)
        addQuad(list,
            cx - s, cy + s, cz - s,
            cx - s, cy + s, cz + s,
            cx - s, cy - s, cz + s,
            cx - s, cy - s, cz - s,
            -1f, 0f, 0f, sideUV, 0.65f
        )

        val buf = ByteBuffer.allocateDirect(list.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        for (f in list) buf.put(f)
        buf.position(0)
        return buf
    }

    /**
     * Builds Steve's classic player forearm reaching towards the held block.
     */
    private fun buildArmMesh(): FloatBuffer {
        val list = ArrayList<Float>(36 * Chunk.FLOATS_PER_VERTEX)
        val w = 0.08f
        val h = 0.08f
        val len = 0.35f

        // Arm extending from bottom-back towards the held block
        val x0 = 0.05f
        val x1 = x0 + w
        val y0 = -0.32f
        val y1 = y0 + h
        val z0 = 0.12f
        val z1 = z0 - len

        // Use oak wood planks tile (tile 8) or smooth wood for arm texture
        val armUV = TextureAtlas.getTileUV(BlockType.WOOD_PLANKS.topTex)

        // Top
        addQuad(list, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0f, 1f, 0f, armUV, 0.95f)
        // Bottom
        addQuad(list, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0f, -1f, 0f, armUV, 0.5f)
        // Front (+Z)
        addQuad(list, x1, y1, z0, x0, y1, z0, x0, y0, z0, x1, y0, z0, 0f, 0f, 1f, armUV, 0.8f)
        // Back (-Z)
        addQuad(list, x0, y1, z1, x1, y1, z1, x1, y0, z1, x0, y0, z1, 0f, 0f, -1f, armUV, 0.8f)
        // Right (+X)
        addQuad(list, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0, 1f, 0f, 0f, armUV, 0.7f)
        // Left (-X)
        addQuad(list, x0, y1, z1, x0, y1, z0, x0, y0, z0, x0, y0, z1, -1f, 0f, 0f, armUV, 0.7f)

        val buf = ByteBuffer.allocateDirect(list.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        for (f in list) buf.put(f)
        buf.position(0)
        return buf
    }

    private fun addQuad(
        list: ArrayList<Float>,
        tlX: Float, tlY: Float, tlZ: Float,
        trX: Float, trY: Float, trZ: Float,
        brX: Float, brY: Float, brZ: Float,
        blX: Float, blY: Float, blZ: Float,
        nx: Float, ny: Float, nz: Float,
        uv: FloatArray,
        ao: Float
    ) {
        val uMin = uv[0]
        val vMin = uv[1]
        val uMax = uv[2]
        val vMax = uv[3]

        addVertex(list, tlX, tlY, tlZ, nx, ny, nz, uMin, vMin, ao)
        addVertex(list, blX, blY, blZ, nx, ny, nz, uMin, vMax, ao)
        addVertex(list, brX, brY, brZ, nx, ny, nz, uMax, vMax, ao)

        addVertex(list, tlX, tlY, tlZ, nx, ny, nz, uMin, vMin, ao)
        addVertex(list, brX, brY, brZ, nx, ny, nz, uMax, vMax, ao)
        addVertex(list, trX, trY, trZ, nx, ny, nz, uMax, vMin, ao)
    }

    private inline fun addVertex(
        list: ArrayList<Float>,
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        u: Float, v: Float,
        ao: Float
    ) {
        list.add(x)
        list.add(y)
        list.add(z)
        list.add(nx)
        list.add(ny)
        list.add(nz)
        list.add(u)
        list.add(v)
        list.add(ao)
    }
}
