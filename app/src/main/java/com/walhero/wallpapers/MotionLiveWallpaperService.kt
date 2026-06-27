package com.walhero.wallpapers

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class MotionLiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = MotionEngine()

    private inner class MotionEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) handler.postDelayed(this, 16L)
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (isVisible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
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
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    val prefs = getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
                    val design = prefs.getString("live_design", "neon_waves") ?: "neon_waves"
                    val title = prefs.getString("live_title", "3D Live Wallpaper") ?: "3D Live Wallpaper"
                    val t = System.currentTimeMillis() / 1000f
                    DemoArt.draw(canvas, canvas.width.toFloat(), canvas.height.toFloat(), design, t, title)
                }
            } catch (_: Exception) {
            } finally {
                if (canvas != null) {
                    try { surfaceHolder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
                }
            }
        }
    }
}
