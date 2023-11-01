package com.example.projectgl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

fun vecLength(x: Double, y: Double, z: Double): Double{
    return sqrt(x*x + y*y + z*z)
}

class LightingShader(private val context: Context, renderer : OpenGLRenderer) {

    private var programId = 0
    private var render = renderer
    private val mModelMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)
    private val color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    private val lightColor = floatArrayOf(1.0f, 0.81f, 0.5f, 1.0f)
    private val lightOrigin = floatArrayOf(0f, 0f, 0f, 1.0f)
    private var lightPos = floatArrayOf(0f, 0f, 0f, 1.0f)
    private var viewPos = floatArrayOf(0.0f, 0.0f, 3.0f, 1.0f)
    private var lightMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val TIME: Long = 10000

    private var colorLocation = 0
    private var lightColorLocation = 0
    private var lightPosLocation = 0
    private var viewPosLocation = 0
    private var mvpMatrixLocation = 0
    private var modelMatrixLocation = 0
    private var positionLocation = 0
    private var normalLocation = 0

    private val vertices: MutableList<Float> = mutableListOf()
    private val indices: MutableList<Short> = mutableListOf()
    private val normCoords: MutableList<Float> = mutableListOf()

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var normBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer


    init {

        createProgram()
        GLES20.glUseProgram(programId)
        getLocations()
        prepareSphere()
    }

    private fun createProgram(){
        val vertexShaderId =
            ShaderUtils.createShader(context, GLES20.GL_VERTEX_SHADER, R.raw.lighting_vertex_shader)
        val fragmentShaderId =
            ShaderUtils.createShader(context, GLES20.GL_FRAGMENT_SHADER, R.raw.lighting_fragment_shader)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
    }
    private fun getLocations() {
        colorLocation = GLES20.glGetUniformLocation(programId, "objectColor")
        lightColorLocation = GLES20.glGetUniformLocation(programId, "lightColor")
        lightPosLocation = GLES20.glGetUniformLocation(programId, "lightPos")
        viewPosLocation = GLES20.glGetUniformLocation(programId, "viewPos")
        mvpMatrixLocation = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        modelMatrixLocation = GLES20.glGetUniformLocation(programId, "model")
        positionLocation = GLES20.glGetAttribLocation(programId, "vPosition")
        normalLocation = GLES20.glGetAttribLocation(programId, "normal")
    }

    private fun prepareSphere()
    {
        var radius = 1;
        var stackCount = 26
        var sectorCount = 26
        var cX = 0
        var cY = 0
        var cZ = 0
        val stackStep = Math.PI / stackCount
        val sectorStep = 2 * Math.PI / sectorCount
        for(i in 0 .. stackCount){
            val stackAngle = Math.PI / 2 - i * stackStep
            val xz = radius * cos(stackAngle)
            val y = radius * sin(stackAngle)
            for(j in 0 .. sectorCount){
                val sectorAngle = j * sectorStep
                val x = xz * sin(sectorAngle)
                val z = xz * cos(sectorAngle)
                vertices.add((x + cX).toFloat())
                vertices.add((y + cY).toFloat())
                vertices.add((z + cZ).toFloat())

                var normX = cX - x
                var normY = cY - y
                var normZ = cZ - z

                val len = vecLength(normX, normY, normZ)
                normX /= len
                normY /= len
                normZ /= len

                normCoords.add(normX.toFloat())
                normCoords.add(normY.toFloat())
                normCoords.add(normZ.toFloat())
            }
        }

        for(i in 0 until stackCount){
            var k1 = i * (sectorCount + 1)
            var k2 = k1 + sectorCount + 1
            for(j in 0 until sectorCount){
                if(i != 0){
                    indices.add(k1.toShort())
                    indices.add(k2.toShort())
                    indices.add((k1 + 1).toShort())
                }
                if((i + 1) != stackCount){
                    indices.add((k1 + 1).toShort())
                    indices.add(k2.toShort())
                    indices.add((k2 + 1).toShort())
                }
                k1++
                k2++
            }
        }

        vertexBuffer =
            ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertices.toFloatArray())
                    position(0)
                }
            }

        normBuffer =
            ByteBuffer.allocateDirect(normCoords.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(normCoords.toFloatArray())
                    position(0)
                }
            }

        indexBuffer =
            ByteBuffer.allocateDirect(indices.size * Short.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    put(indices.toShortArray())
                    position(0)
                }
            }

    }

    private fun bindDataSphere() {
        GLES20.glVertexAttribPointer(
            positionLocation,
            3,
            GLES20.GL_FLOAT,
            false,
            3 * Float.SIZE_BYTES,
            vertexBuffer
        )

        GLES20.glVertexAttribPointer(
            normalLocation,
            3,
            GLES20.GL_FLOAT,
            false,
            3 * Float.SIZE_BYTES,
            normBuffer
        )

        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glEnableVertexAttribArray(normalLocation)

//        viewPos[0] = render.vX
//        viewPos[1] = render.vY
//        viewPos[2] = render.vZ

        GLES20.glUniform4fv(colorLocation, 1, color, 0)
        GLES20.glUniform4fv(lightColorLocation, 1, lightColor, 0)
        Matrix.multiplyMV(lightPos, 0, lightMatrix, 0, lightOrigin, 0)
        GLES20.glUniform4fv(lightPosLocation, 1, lightPos, 0)
        GLES20.glUniform4fv(viewPosLocation, 1, viewPos, 0)


    }

    public fun bindMatrix(
        mViewMatrix: FloatArray,
        mProjectionMatrix: FloatArray
    ) {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
//        GLES20.glUniformMatrix4fv(mvpMatrixLocation, 1, false, mMatrix, 0)
//        Matrix.setIdentityM(mvpMatrix, 0)
//        Matrix.multiplyMM(mvpMatrix, 0, mViewMatrix, 0,mModelMatrix , 0);
        GLES20.glUniformMatrix4fv(mvpMatrixLocation, 1, false, mMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixLocation, 1, false, mModelMatrix, 0)

    }

    public fun draw(
        mViewMatrix: FloatArray,
        mProjectionMatrix: FloatArray)
    {
        GLES20.glUseProgram(programId)
        Matrix.setIdentityM(mModelMatrix, 0);

        bindDataSphere()
        //setModelMatrixSphere()
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

    }

    private fun setModelMatrixSphere() {
        Matrix.setIdentityM(mModelMatrix, 0);
        val angle = (SystemClock.uptimeMillis() % TIME).toFloat() / TIME * 360
        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.scaleM(mModelMatrix, 0, 0.2f, 0.2f, 0.2f);
        Matrix.translateM(mModelMatrix, 0, -10f, 0f, 0f);
    }

    public fun setLightPos(x : Float, y: Float, z: Float)
    {
        lightPos = floatArrayOf(x,y,z)
    }

    public fun setLightMatrix(lMatrix : FloatArray)
    {
        lightMatrix = lMatrix
    }
}