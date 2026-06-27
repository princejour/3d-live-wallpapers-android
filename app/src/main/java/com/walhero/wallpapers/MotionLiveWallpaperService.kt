package com.walhero.wallpapers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
                if (canvas != null) drawWallpaper(canvas)
            } catch (_: Exception) {
            } finally {
                if (canvas != null) {
                    try { surfaceHolder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
                }
            }
        }

        private fun drawWallpaper(canvas: Canvas) {
            val prefs = getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
            val design = prefs.getString("live_design", "neon_waves") ?: "neon_waves"
            val title = prefs.getString("live_title", "3D Live Wallpaper") ?: "3D Live Wallpaper"
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val t = System.currentTimeMillis() / 1000f

            when (design) {
                "luxury_gold" -> luxuryGold(canvas, w, h, t)
                "neon_waves" -> neonWaves(canvas, w, h, t)
                "emerald_particles" -> emeraldParticles(canvas, w, h, t)
                "rose_hearts" -> roseHearts(canvas, w, h, t)
                "islamic_orbit" -> islamicOrbit(canvas, w, h, t)
                "fire_energy" -> fireEnergy(canvas, w, h, t)
                "galaxy_depth" -> galaxyDepth(canvas, w, h, t)
                "ocean_motion" -> oceanMotion(canvas, w, h, t)
                else -> neonWaves(canvas, w, h, t)
            }
            drawTitle(canvas, w, h, title)
        }

        private fun bg(canvas: Canvas, w: Float, h: Float, colors: IntArray) {
            paint.shader = LinearGradient(0f, 0f, w, h, colors, null, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null
            paint.alpha = 255
            paint.style = Paint.Style.FILL
        }

        private fun luxuryGold(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(3,3,6), Color.rgb(65,40,8), Color.rgb(255,180,45)))
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            paint.color = Color.argb(180, 255, 220, 95)
            val pulse = 12f * sin(t)
            c.drawRoundRect(RectF(w*.14f, h*.18f, w*.86f, h*.78f), 42f+pulse, 42f+pulse, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(120,255,230,120)
            for (i in 0..18) c.drawCircle(w*((i%6)+1)/7f, (h*.18f + ((i*67 + t*90)% (h*.55f))), 3f+i%7, paint)
        }

        private fun neonWaves(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(2,8,35), Color.rgb(0,220,255), Color.rgb(120,80,255)))
            paint.style = Paint.Style.STROKE
            for (layer in 0..5) {
                val p = Path(); val base = h*(.28f + layer*.11f); p.moveTo(0f, base)
                var x = 0f
                while (x <= w) { p.lineTo(x, base + sin(x/w*8f + t*(1.1f+layer*.18f))* (22f+layer*7f)); x += 14f }
                paint.strokeWidth = 4f + layer
                paint.color = Color.argb(150-layer*15,255,255,255)
                c.drawPath(p, paint)
            }
            paint.style = Paint.Style.FILL
        }

        private fun emeraldParticles(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(0,18,14), Color.rgb(0,120,80), Color.rgb(0,235,150)))
            paint.color = Color.argb(150,220,255,235)
            for (i in 0..55) {
                val x = w*((sin(i*9.7f)+1f)/2f) + sin(t+i)*18f
                val y = (h*((cos(i*4.2f)+1f)/2f) + t*(20+i%7*5)) % h
                c.drawCircle(x, y, 2.5f+i%6, paint)
            }
        }

        private fun roseHearts(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(42,5,30), Color.rgb(210,35,105), Color.rgb(255,100,165)))
            paint.color = Color.argb(130,255,255,255)
            for (i in 0..14) drawHeart(c, w*(.14f+i*.06f), (h*.82f - ((t*70+i*85)% (h*.68f))), 12f+i%7, paint)
        }

        private fun islamicOrbit(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(0,8,22), Color.rgb(0,85,80), Color.rgb(210,165,65)))
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = Color.argb(180,255,225,130)
            for (i in 0..7) c.drawCircle(w*.5f, h*.42f, w*(.11f+i*.045f)+sin(t+i)*8f, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(230,255,230,150)
            c.drawCircle(w*.47f, h*.36f, w*.11f, paint)
            paint.color = Color.rgb(0,35,45)
            c.drawCircle(w*.52f, h*.34f, w*.11f, paint)
            paint.color = Color.argb(190,255,255,255)
            for (i in 0..22) c.drawCircle(w*((sin(i*13f+t*.2f)+1)/2f), h*((cos(i*7f+t*.15f)+1)/2f), 2f+i%3, paint)
        }

        private fun fireEnergy(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(12,2,0), Color.rgb(130,20,0), Color.rgb(255,145,20)))
            for (i in 0..15) {
                val p = Path(); val x = w*(.05f+i*.065f); val top = h*(.28f+.08f*sin(t+i))
                p.moveTo(x, top); p.cubicTo(x-55f,h*.58f,x-20f,h*.75f,x,h*.92f); p.cubicTo(x+55f,h*.62f,x+25f,h*.47f,x,top)
                paint.color = Color.argb(70+i%5*18,255,230,90); c.drawPath(p, paint)
            }
        }

        private fun galaxyDepth(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(3,2,22), Color.rgb(65,20,100), Color.rgb(0,170,230)))
            paint.color = Color.argb(65,190,90,255)
            c.drawCircle(w*(.58f+.04f*sin(t*.4f)), h*.42f, w*.48f, paint)
            paint.color = Color.WHITE
            for (i in 0..70) { paint.alpha = 70+i%160; c.drawCircle(w*((sin(i*12.2f+t*.3f)+1)/2f), h*((cos(i*8.7f+t*.25f)+1)/2f), 1.5f+i%4, paint) }
            paint.alpha = 255
        }

        private fun oceanMotion(c: Canvas, w: Float, h: Float, t: Float) {
            bg(c, w, h, intArrayOf(Color.rgb(0,35,70), Color.rgb(0,165,205), Color.rgb(160,245,255)))
            for (layer in 0..5) {
                val p = Path(); val base = h*(.38f+layer*.09f); p.moveTo(0f,h); p.lineTo(0f,base)
                var x = 0f
                while (x <= w) { p.lineTo(x, base + sin(x/w*7f+t+layer)* (20f+layer*5f)); x += 14f }
                p.lineTo(w,h); p.close(); paint.color = Color.argb(70+layer*22,255,255,255); c.drawPath(p, paint)
            }
        }

        private fun drawHeart(c: Canvas, x: Float, y: Float, s: Float, pnt: Paint) {
            val p = Path(); p.moveTo(x, y+s); p.cubicTo(x-2*s,y-.5f*s,x-s,y-2*s,x,y-s); p.cubicTo(x+s,y-2*s,x+2*s,y-.5f*s,x,y+s); c.drawPath(p,pnt)
        }

        private fun drawTitle(c: Canvas, w: Float, h: Float, title: String) {
            paint.shader = null; paint.style = Paint.Style.FILL; paint.color = Color.WHITE; paint.alpha = 235
            paint.textSize = w.coerceAtMost(h)*.045f; paint.isFakeBoldText = true; paint.setShadowLayer(18f,0f,4f,Color.argb(190,0,0,0))
            c.drawText(title.take(26), w*.07f, h*.82f, paint)
            paint.isFakeBoldText = false; paint.textSize = w.coerceAtMost(h)*.026f
            c.drawText("Animated live wallpaper", w*.07f, h*.855f, paint)
            paint.clearShadowLayer(); paint.alpha = 255
        }
    }
}
