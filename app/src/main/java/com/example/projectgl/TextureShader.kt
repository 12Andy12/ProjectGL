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

    private lateinit var vertexDataSphere: FloatBuffer

    private val mMatrix = FloatArray(16)

    private var textureBox = 0
    private var textureSun = 0
    private var textureEarth = 0
    private var textureTest = 0
    private var textureMoon = 0

    private val TIME: Long = 10000

    private var sphereVertexCount = 0

    init {
        createProgram()
        GLES20.glUseProgram(programId)
        getLocations()
        prepareData()
        prepareSphere()
        prepareTextures()
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

    private fun prepareTextures()
    {
        textureBox = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.box0, R.drawable.box1,
                R.drawable.box2, R.drawable.box3,
                R.drawable.box4, R.drawable.box5
            )
        )

        textureSun = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.sun, R.drawable.sun,
                R.drawable.sun, R.drawable.sun,
                R.drawable.sun, R.drawable.sun
            )
        )

        textureEarth = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.earth_left, R.drawable.earth_right,
                R.drawable.earth_bot, R.drawable.earth_top,
                R.drawable.earth_back, R.drawable.earth_mid
            )
        )

        textureMoon = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.moon, R.drawable.moon,
                R.drawable.moon, R.drawable.moon,
                R.drawable.moon, R.drawable.moon
            )
        )

        textureTest = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.test_right, R.drawable.test_left,
                R.drawable.test_bottom, R.drawable.test_top,
                R.drawable.test_back, R.drawable.test_center
            )
        )

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

        // помещаем текстуру в target CUBE_MAP юнита 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);


        // юнит текстуры
        GLES20.glUniform1i(uTextureUnitLocation, 0);
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

        // помещаем текстуру в target CUBE_MAP юнита 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);


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
        GLES20.glUseProgram(programId)
        Matrix.setIdentityM(mModelMatrix, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureBox);
        bindData()
        setModelMatrixBox()
        bindMatrix(mViewMatrix, mProjectionMatrix)



        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, indexArray);

        bindDataSphere()

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureEarth);
        setModelMatrixEarth()
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sphereVertexCount);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureTest);
        setModelMatrixTest()
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sphereVertexCount);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureSun);
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sphereVertexCount);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureMoon);
        setModelMatrixMoon()
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sphereVertexCount);

    }

    private fun setModelMatrixBox() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, 0.2f, 0.2f, 0.2f);
        Matrix.translateM(mModelMatrix, 0, 20f, 0f, 0f);
    }

    private fun setModelMatrixEarth() {
        Matrix.setIdentityM(mModelMatrix, 0);
        val angle = (SystemClock.uptimeMillis() % TIME).toFloat() / TIME * 360
        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.scaleM(mModelMatrix, 0, 0.2f, 0.2f, 0.2f);
        Matrix.translateM(mModelMatrix, 0, -10f, 0f, 0f);
    }

    private fun setModelMatrixMoon() {
        Matrix.setIdentityM(mModelMatrix, 0);
        val angle = (SystemClock.uptimeMillis() % TIME).toFloat() / TIME * 360
        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.rotateM(mModelMatrix, 0, angle, 1f, 0f, 0f)
        Matrix.scaleM(mModelMatrix, 0, 0.05f, 0.05f, 0.05f);
        Matrix.translateM(mModelMatrix, 0, -40f, 8f, 3f);
        Matrix.rotateM(mModelMatrix, 0, angle, 1f, 0f, 0f)
    }

    private fun setModelMatrixTest() {
        Matrix.setIdentityM(mModelMatrix, 0);
        val t = TIME*2;
        val angle = (SystemClock.uptimeMillis() % t).toFloat() / t * 360
        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f);
        Matrix.translateM(mModelMatrix, 0, -6f, 0f, 2f);
        //Matrix.rotateM(mModelMatrix, 0, angle*1.5f, 1f, 1f, 1f)
    }
}
