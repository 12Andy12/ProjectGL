package com.example.projectgl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils


object TextureUtils {
    fun loadTexture(context: Context, resourceId: Int): Int {
        // создание объекта текстуры
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            return 0
        }

        // получение Bitmap
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(
            context.resources, resourceId, options
        )
        if (bitmap == null) {
            GLES20.glDeleteTextures(1, textureIds, 0)
            return 0
        }

        // настройка объекта текстуры
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        // сброс target
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureIds[0]
    }
    fun loadTextureCube(context: Context, resourceId: IntArray): Int {
        // создание объекта текстуры
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            return 0
        }

        // получение Bitmap
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmaps = arrayOfNulls<Bitmap>(6)
        for (i in 0..5) {
            bitmaps[i] = BitmapFactory.decodeResource(
                context.resources, resourceId[i], options
            )
            if (bitmaps[i] == null) {
                GLES20.glDeleteTextures(1, textureIds, 0)
                return 0
            }
        }

        // настройка объекта текстуры
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureIds[0])
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_CUBE_MAP,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_CUBE_MAP,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, bitmaps[0], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, bitmaps[1], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, bitmaps[2], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, bitmaps[3], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, bitmaps[4], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, bitmaps[5], 0)
        for (bitmap in bitmaps) {
            bitmap!!.recycle()
        }

        // сброс target
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0)
        return textureIds[0]
    }
}