package com.example.projectgl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUseProgram
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.example.projectgl.ShaderUtils.createProgram
import com.example.projectgl.ShaderUtils.createShader
import com.example.projectgl.TextureUtils.loadTextureCube
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin


class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)

    private val TIME: Long = 10000

    private lateinit var colorShader : ColorShader
    private lateinit var textureShader : TextureShader



    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        colorShader = ColorShader(context)
        textureShader = TextureShader(context)

        createViewMatrix()
    }


    override fun onSurfaceChanged(arg0: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        createProjectionMatrix(width, height);
        colorShader.bindMatrix(mViewMatrix, mProjectionMatrix)
        textureShader.bindMatrix(mViewMatrix, mProjectionMatrix)
    }

    private fun createProjectionMatrix(width: Int, height: Int) {
        var ratio: Float
        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 2f
        val far = 16f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    private fun createViewMatrix() {
        val t = TIME * 3
        val time = (SystemClock.uptimeMillis() % t).toFloat() / t
        val angle: Double = time * 2 * 3.1415926

        // точка положения камеры
        val eyeX = (cos(angle) * 6f).toFloat();
        val eyeY = 2f //(cos(angle) * 3f).toFloat();
        val eyeZ = (sin(angle) * 6f).toFloat();

        // точка направления камеры
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // up-вектор
        val vX = 0f
        val vY = 1f
        val vZ = 0f
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, vX, vY, vZ)
    }

    override fun onDrawFrame(arg0: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        createViewMatrix();

        textureShader.draw(mViewMatrix, mProjectionMatrix)
        colorShader.draw(mViewMatrix, mProjectionMatrix)
    }


}