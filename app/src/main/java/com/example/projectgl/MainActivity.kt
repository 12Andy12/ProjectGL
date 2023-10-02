package com.example.projectgl

//import android.support.v7.app.AppCompatActivity
//import kotlinx.android.synthetic.main.activity_main.*

import android.app.Activity
import android.app.ActivityManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!supportES2()) {
            Toast.makeText(this, "OpenGl ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        var glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(OpenGLRenderer(this))
        setContentView(glSurfaceView)
    }

    private fun supportES2(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x20000
    }

    class MyRenderer : GLSurfaceView.Renderer {
        var a = floatArrayOf(
            -1f, 1f, 0f,
            -1f, -1f, 0f,
            1f, -1f, 0f,
            1f, 1f, 0f
        )
        var f: FloatBuffer
        var b: ByteBuffer

        init {
            b = ByteBuffer.allocateDirect(4 * 3 * 4)
            b.order(ByteOrder.nativeOrder())
            f = b.asFloatBuffer()
            f.put(a)
            f.position(0)
        }

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
            gl.glClearColor(1f, 1f, 0f, 1f)
        }
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
        override fun onDrawFrame(gl: GL10) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
            gl.glLoadIdentity()
            gl.glTranslatef(0f, 0f, -1f)
            gl.glScalef(0.5f, 0.5f, 0.5f)
            gl.glColor4f(0f, 1f, 1f, 1f)
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, f)
            gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4)
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        }
    }
}
