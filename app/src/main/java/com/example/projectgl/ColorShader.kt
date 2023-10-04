package com.example.projectgl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glUseProgram
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class ColorShader(private val context: Context) {
    private var programId = 0

    private val mModelMatrix = FloatArray(16)

    private var aColorLocation = 0
    private var aPositionLocation = 0
    private var uMatrixLocation = 0

    private lateinit var vertexData: FloatBuffer
    private lateinit var indexArrayCoordinates: ByteBuffer
    private lateinit var indexArraySquere: ByteBuffer

    private lateinit var vertexDataSphere: FloatBuffer

    private val mMatrix = FloatArray(16)

    private val TIME: Long = 10000

    private var sphereVertexCount = 0

    init {
        createProgram()
        glUseProgram(programId)
        getLocations()
        prepareData()
        prepareSphere()
    }

    private fun createProgram() {
        val vertexShaderId =
            ShaderUtils.createShader(context, GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader2)
        val fragmentShaderId =
            ShaderUtils.createShader(context, GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader2)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
    }

    private fun getLocations() {
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        aColorLocation = GLES20.glGetAttribLocation(programId, "a_Color")
        uMatrixLocation = GLES20.glGetUniformLocation(programId, "u_Matrix")
    }

    private fun prepareData() {
        val vertices = floatArrayOf(
            // ось X
            -10f, 0f, 0f, 1f, 0f, 0f,
            10f, 0f, 0f, 1f, 0f, 0f,

            // ось Y
            0f, -10f, 0f, 0f, 1f, 0f,
            0f, 10f, 0f, 0f, 1f, 0f,

            // ось Z
            0f, 0f, -10f, 0f, 0f, 1f,
            0f, 0f, 10f, 0f, 0f, 1f,

            //квадрат
            0.5f, 0.5f, 0f, 1f, 0f, 0f,
            -0.5f, 0.5f, 0f, 1f, 0f, 0f,
            -0.5f, -0.5f, 0f, 0f, 1f, 0f,
            0.5f, -0.5f, 0f, 0f, 1f, 0f
        )
        vertexData = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexData.put(vertices)
        indexArrayCoordinates = ByteBuffer.allocateDirect(6)
            .put(
                byteArrayOf(
                    0, 1,
                    2, 3,
                    4, 5
                )
            )
        indexArrayCoordinates.position(0)

        indexArraySquere = ByteBuffer.allocateDirect(6)
            .put(
                byteArrayOf(
                    6,7,8,
                    8,9,6
                )
            )
        indexArraySquere.position(0)
    }

    private fun prepareSphere() {

        var gradation = 10
        var vertices = floatArrayOf()
        val PI = 3.141592f
        var alpha = 0f
        var beta: Float
        var radius = 1f

        while (alpha < 3.14f) {
            beta = 0f
            while (beta < 2.01f * 3.14f) {
                var x = (radius * cos(beta) * sin(alpha));
                var y = (radius * sin(beta) * sin(alpha));
                var z = (radius * cos(alpha));
                vertices += x.toFloat()
                vertices += y.toFloat()
                vertices += z.toFloat()
                vertices += 0f
                vertices += 1f
                vertices += 0f

                x = (radius * cos(beta) * sin(alpha + PI / gradation))
                y = (radius * sin(beta) * sin(alpha + PI / gradation))
                z = (radius * cos(alpha + PI / gradation))

                vertices += x.toFloat()
                vertices += y.toFloat()
                vertices += z.toFloat()
                vertices += 1f
                vertices += 0f
                vertices += 1f

                beta += PI / gradation
            }

            alpha += PI / gradation
        }
        println("------------------------------------------------------------")
        var count = 1
        for (i in vertices) {
            print("$i ")
            if (count % 6 == 0)
                print("\n")
            count += 1
        }
        sphereVertexCount = vertices.size  / 6
        println("------------------------------------------------------------")
        println(count / 6)
        println(sphereVertexCount)
        vertexDataSphere = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexDataSphere.put(vertices)
    }

    private fun bindDataSphere() {
        // координаты
        vertexDataSphere.position(0);
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            3,
            GLES20.GL_FLOAT,
            false,
            24,
            vertexDataSphere
        );
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        // цвет
        vertexDataSphere.position(3);
        GLES20.glVertexAttribPointer(
            aColorLocation,
            3,
            GLES20.GL_FLOAT,
            false,
            24,
            vertexDataSphere
        );
        GLES20.glEnableVertexAttribArray(aColorLocation);
    }

    private fun bindData() {
        // координаты
        vertexData.position(0);
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            3,
            GLES20.GL_FLOAT,
            false,
            24,
            vertexData
        );
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        // цвет
        vertexData.position(3);
        GLES20.glVertexAttribPointer(
            aColorLocation,
            3,
            GLES20.GL_FLOAT,
            false,
            24,
            vertexData
        );
        GLES20.glEnableVertexAttribArray(aColorLocation);
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
        mProjectionMatrix: FloatArray
    ) {
        Matrix.setIdentityM(mModelMatrix, 0);
        bindData()
        glUseProgram(programId)
        bindMatrix(mViewMatrix, mProjectionMatrix)

        GLES20.glLineWidth(10f)
        GLES20.glDrawElements(GLES20.GL_LINES, 6, GLES20.GL_UNSIGNED_BYTE, indexArrayCoordinates);

        setModelMatrixSquare()
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_BYTE, indexArraySquere);


        bindDataSphere()

        setModelMatrixChupaChups()
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sphereVertexCount);
    }

    private fun setModelMatrixChupaChups() {
        Matrix.setIdentityM(mModelMatrix, 0);
        val angle = (SystemClock.uptimeMillis() % TIME).toFloat() / TIME * 360

        Matrix.translateM(mModelMatrix, 0, 0f, 0f, 6f);
        Matrix.rotateM(mModelMatrix, 0, angle, 1f, 1f, 0f)
    }

    private fun setModelMatrixSquare() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f);
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, 8f);
    }
}
