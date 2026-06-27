package com.walhero.wallpapers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.cos
import kotlin.math.sin

class MotionLiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = MotionEngine()

    private inner class MotionEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var visible = false

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) handler.postDelayed(this, 16L)
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (isVisible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            visible = true
            handler.post(drawRunner)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunner)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas == null) return
                drawWallpaper(canvas)
            } catch (_: Exception) {
                // Surface can disappear while the wallpaper picker is closing.
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        private fun drawWallpaper(canvas: Canvas) {
            val prefs = getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
            val c0 = prefs.getInt("live_color_0", Color.rgb(5, 8, 22))
            val c1 = prefs.getInt("live_color_1", Color.rgb(0, 229, 255))
            val c2 = prefs.getInt("live_color_2", Color.rgb(108, 99, 255))
            val title = prefs.getString("live_title", "3D Motion Live Wallpaper") ?: "3D Motion Live Wallpaper"

            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val time = System.currentTimeMillis() / 1000f

            paint.shader = LinearGradient(0f, 0f, width, height, intArrayOf(c0, c1, c2), null, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, width, height, paint)
            paint.shader = null

            drawGlow(canvas, width, height, time, c1)
            drawWave(canvas, width, height, time, c2)
            drawParticles(canvas, width, height, time, c1)
            drawTitle(canvas, width, height, title)
        }

        private fun drawGlow(canvas: Canvas, width: Float, height: Float, time: Float, color: Int) {
            paint.color = color
            paint.alpha = 45
            for (i in 0..3) {
                val x = width * (0.25f + 0.18f * i + 0.05f * sin(time + i))
                val y = height * (0.25f + 0.13f * i + 0.04f * cos(time * 0.8f + i))
                canvas.drawCircle(x, y, width * (0.25f + i * 0.04f), paint)
            }
            paint.alpha = 255
        }

        private fun drawWave(canvas: Canvas, width: Float, height: Float, time: Float, color: Int) {
            val path = Path()
            val baseY = height * 0.62f
            path.moveTo(0f, baseY)
            var x = 0f
            while (x <= width) {
                val y = baseY + sin((x / width * 6.2f) + time * 1.35f) * 54f + cos((x / width * 9.1f) + time) * 24f
                path.lineTo(x, y)
                x += 18f
            }
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
            paint.color = color
            paint.alpha = 90
            canvas.drawPath(path, paint)
            paint.alpha = 255
        }

        private fun drawParticles(canvas: Canvas, width: Float, height: Float, time: Float, color: Int) {
            paint.color = Color.WHITE
            for (i in 0..34) {
                val seed = i * 37.71f
                val x = ((sin(seed) * 0.5f + 0.5f) * width + sin(time * 0.7f + i) * 28f)
                val rawY = (cos(seed * 1.7f) * 0.5f + 0.5f) * height
                val y = (rawY + time * (18f + i % 5 * 4f)) % height
                val radius = 2.5f + (i % 6)
                paint.alpha = 60 + (i % 7) * 20
                canvas.drawCircle(x, y, radius, paint)
            }
            paint.color = color
            paint.alpha = 70
            for (i in 0..10) {
                val x = width * (i / 10f)
                val y = height * 0.16f + sin(time + i) * 45f
                canvas.drawCircle(x, y, 18f + i, paint)
            }
            paint.alpha = 255
        }

        private fun drawTitle(canvas: Canvas, width: Float, height: Float, title: String) {
            paint.shader = null
            paint.color = Color.WHITE
            paint.alpha = 230
            paint.textSize = width.coerceAtMost(height) * 0.045f
            paint.isFakeBoldText = true
            paint.setShadowLayer(18f, 0f, 4f, Color.argb(190, 0, 0, 0))
            canvas.drawText(title.take(26), width * 0.07f, height * 0.82f, paint)
            paint.isFakeBoldText = false
            paint.textSize = width.coerceAtMost(height) * 0.026f
            canvas.drawText("Live • Native Kotlin", width * 0.07f, height * 0.855f, paint)
            paint.clearShadowLayer()
            paint.alpha = 255
        }
    }
}
