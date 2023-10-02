package com.example.projectgl

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class TextureShader(private val context: Context) {
    private var programId = 0

    private val mModelMatrix = FloatArray(16)

    private var uTextureUnitLocation = 0
    private var aPositionLocation = 0
    private var uMatrixLocation = 0

    private lateinit var vertexData: FloatBuffer
    private lateinit var indexArray: ByteBuffer

    private val mMatrix = FloatArray(16)

    private var texture = 0

    private val TIME: Long = 10000

    init {
        createProgram()
        GLES20.glUseProgram(programId)
        getLocations()
        prepareData()
    }
    private fun createProgram(){
        val vertexShaderId =
            ShaderUtils.createShader(context, GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShaderId =
            ShaderUtils.createShader(context, GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
    }
    private fun getLocations() {
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        uTextureUnitLocation = GLES20.glGetUniformLocation(programId, "u_TextureUnit")
        uMatrixLocation = GLES20.glGetUniformLocation(programId, "u_Matrix")
    }

    private fun prepareData()
    {
        val vertices = floatArrayOf(
            // вершины куба
            -1f,  1f,  1f,     // верхняя левая ближняя
            1f,  1f,  1f,     // верхняя правая ближняя
            -1f, -1f,  1f,     // нижняя левая ближняя
            1f, -1f,  1f,     // нижняя правая ближняя
            -1f,  1f, -1f,     // верхняя левая дальняя
            1f,  1f, -1f,     // верхняя правая дальняя
            -1f, -1f, -1f,     // нижняя левая дальняя
            1f, -1f, -1f      // нижняя правая дальняя

        )
        vertexData = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexData.put(vertices)

        indexArray = ByteBuffer.allocateDirect(36)
            .put(
                byteArrayOf( // грани куба
                    // ближняя
                    1, 3, 0,
                    0, 3, 2,  // дальняя
                    4, 6, 5,
                    5, 6, 7,  // левая
                    0, 2, 4,
                    4, 2, 6,  // правая
                    5, 7, 1,
                    1, 7, 3,  // верхняя
                    5, 1, 4,
                    4, 1, 0,  // нижняя
                    6, 2, 7,
                    7, 2, 3
                )
            )
        indexArray.position(0)

        texture = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.box0, R.drawable.box1,
                R.drawable.box2, R.drawable.box3,
                R.drawable.box4, R.drawable.box5
            )
        )
    }

    private fun bindData()
    {
        // координаты
        vertexData.position(0);
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexData
        );
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        // юнит текстуры
        GLES20.glUniform1i(uTextureUnitLocation, 0);
    }

    public fun bindMatrix(
        mViewMatrix: FloatArray,
        mProjectionMatrix: FloatArray
    ) {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

    public fun draw(
        mViewMatrix: FloatArray,
        mProjectionMatrix: FloatArray)
    {
        Matrix.setIdentityM(mModelMatrix, 0);
        bindData()
        GLES20.glUseProgram(programId)
        setModelMatrix()
        bindMatrix(mViewMatrix, mProjectionMatrix)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, texture);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, indexArray);

    }

    private fun setModelMatrix() {
//        val angle = (SystemClock.uptimeMillis() % TIME).toFloat() / TIME * 360
//        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 1f, 1f)
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f);
        Matrix.translateM(mModelMatrix, 0, 3f, 0f, 0f);
    }
}
