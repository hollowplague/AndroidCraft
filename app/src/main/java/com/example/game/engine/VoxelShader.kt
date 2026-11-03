package com.example.game.engine

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * High-performance GLSL shaders for 3D Minecraft voxel rendering:
 * directional sunlight, face ambient occlusion, atmospheric distance fog, and selection wireframes.
 */
class VoxelShader {

    companion object {
        private const val TAG = "VoxelShader"

        private const val VERTEX_SHADER = """
            uniform mat4 u_MVPMatrix;
            uniform vec3 u_CameraPos;
            uniform vec3 u_SunDir;
            uniform vec3 u_SunColor;
            uniform vec3 u_AmbientColor;
            
            attribute vec3 a_Position;
            attribute vec3 a_Normal;
            attribute vec2 a_TexCoord;
            attribute float a_AO;
            
            varying vec2 v_TexCoord;
            varying vec3 v_Lighting;
            varying float v_Distance;

            void main() {
                gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);
                v_TexCoord = a_TexCoord;

                // Diffuse directional sunlight calculation
                float diffuse = max(dot(a_Normal, u_SunDir), 0.0);
                vec3 light = u_AmbientColor + (u_SunColor * diffuse);

                // Apply per-vertex ambient occlusion
                v_Lighting = light * a_AO;

                // Distance to camera for atmospheric fog
                v_Distance = length(u_CameraPos - a_Position);
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D u_Texture;
            uniform vec3 u_FogColor;
            uniform float u_FogStart;
            uniform float u_FogEnd;
            uniform float u_AlphaMultiplier;

            varying vec2 v_TexCoord;
            varying vec3 v_Lighting;
            varying float v_Distance;

            void main() {
                vec4 texColor = texture2D(u_Texture, v_TexCoord);

                // Alpha cutout discard (leaves, clear glass)
                if (texColor.a < 0.1) {
                    discard;
                }

                // Apply lighting
                vec3 rgb = texColor.rgb * v_Lighting;

                // Atmospheric exponential distance fog
                float fogFactor = clamp((v_Distance - u_FogStart) / (u_FogEnd - u_FogStart), 0.0, 1.0);
                vec3 finalColor = mix(rgb, u_FogColor, fogFactor);

                gl_FragColor = vec4(finalColor, texColor.a * u_AlphaMultiplier);
            }
        """

        private const val WIREFRAME_VERTEX = """
            uniform mat4 u_MVPMatrix;
            attribute vec3 a_Position;
            void main() {
                gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);
            }
        """

        private const val WIREFRAME_FRAGMENT = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """
    }

    var programId = 0
        private set

    var uMVPMatrixLoc = 0
    var uCameraPosLoc = 0
    var uSunDirLoc = 0
    var uSunColorLoc = 0
    var uAmbientColorLoc = 0
    var uTextureLoc = 0
    var uFogColorLoc = 0
    var uFogStartLoc = 0
    var uFogEndLoc = 0
    var uAlphaMultiplierLoc = 0

    var aPositionLoc = 0
    var aNormalLoc = 0
    var aTexCoordLoc = 0
    var aAOLoc = 0

    // Wireframe selection outline program
    var wireframeProgram = 0
    var wfMVPLoc = 0
    var wfColorLoc = 0
    var wfPosLoc = 0

    private var wireframeBuffer: FloatBuffer? = null

    fun initialize() {
        programId = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        uMVPMatrixLoc = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uCameraPosLoc = GLES20.glGetUniformLocation(programId, "u_CameraPos")
        uSunDirLoc = GLES20.glGetUniformLocation(programId, "u_SunDir")
        uSunColorLoc = GLES20.glGetUniformLocation(programId, "u_SunColor")
        uAmbientColorLoc = GLES20.glGetUniformLocation(programId, "u_AmbientColor")
        uTextureLoc = GLES20.glGetUniformLocation(programId, "u_Texture")
        uFogColorLoc = GLES20.glGetUniformLocation(programId, "u_FogColor")
        uFogStartLoc = GLES20.glGetUniformLocation(programId, "u_FogStart")
        uFogEndLoc = GLES20.glGetUniformLocation(programId, "u_FogEnd")
        uAlphaMultiplierLoc = GLES20.glGetUniformLocation(programId, "u_AlphaMultiplier")

        aPositionLoc = GLES20.glGetAttribLocation(programId, "a_Position")
        aNormalLoc = GLES20.glGetAttribLocation(programId, "a_Normal")
        aTexCoordLoc = GLES20.glGetAttribLocation(programId, "a_TexCoord")
        aAOLoc = GLES20.glGetAttribLocation(programId, "a_AO")

        initWireframeShader()
    }

    private fun initWireframeShader() {
        wireframeProgram = createProgram(WIREFRAME_VERTEX, WIREFRAME_FRAGMENT)
        wfMVPLoc = GLES20.glGetUniformLocation(wireframeProgram, "u_MVPMatrix")
        wfColorLoc = GLES20.glGetUniformLocation(wireframeProgram, "u_Color")
        wfPosLoc = GLES20.glGetAttribLocation(wireframeProgram, "a_Position")

        // 12 edges of a 1x1x1 unit cube
        val e = 0.002f // Slight epsilon expansion to prevent z-fighting
        val min = -e
        val max = 1.0f + e

        val lines = floatArrayOf(
            // Bottom 4 edges
            min, min, min,  max, min, min,
            max, min, min,  max, min, max,
            max, min, max,  min, min, max,
            min, min, max,  min, min, min,
            // Top 4 edges
            min, max, min,  max, max, min,
            max, max, min,  max, max, max,
            max, max, max,  min, max, max,
            min, max, max,  min, max, min,
            // Vertical 4 pillars
            min, min, min,  min, max, min,
            max, min, min,  max, max, min,
            max, min, max,  max, max, max,
            min, min, max,  min, max, max
        )

        wireframeBuffer = ByteBuffer.allocateDirect(lines.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(lines)
                position(0)
            }
    }

    fun drawSelectionBox(mvpMatrix: FloatArray) {
        val buf = wireframeBuffer ?: return
        GLES20.glUseProgram(wireframeProgram)
        GLES20.glUniformMatrix4fv(wfMVPLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(wfColorLoc, 0.0f, 0.0f, 0.0f, 0.8f) // Crisp black outline

        GLES20.glEnableVertexAttribArray(wfPosLoc)
        GLES20.glVertexAttribPointer(wfPosLoc, 3, GLES20.GL_FLOAT, false, 0, buf)

        GLES20.glLineWidth(3.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 24)

        GLES20.glDisableVertexAttribArray(wfPosLoc)
    }

    private fun createProgram(vertexSrc: String, fragmentSrc: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSrc)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val log = GLES20.glGetProgramInfoLog(program)
            Log.e(TAG, "Shader link error: $log")
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader compilation error ($type): $log")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }
}
