package com.walhero.wallpapers

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.min

class MainActivity : Activity() {

    private enum class WallpaperType { LIVE, STATIC }

    private data class WallpaperItem(
        val id: String,
        val title: String,
        val type: WallpaperType,
        val category: String,
        val colors: IntArray
    )

    private val wallpapers = listOf(
        WallpaperItem("live_luxury_gold", "Luxury Gold Motion", WallpaperType.LIVE, "Luxury", intArrayOf(Color.rgb(5, 8, 22), Color.rgb(255, 185, 60), Color.rgb(95, 55, 15))),
        WallpaperItem("live_neon_blue", "Neon Blue Waves", WallpaperType.LIVE, "3D", intArrayOf(Color.rgb(3, 7, 30), Color.rgb(0, 229, 255), Color.rgb(108, 99, 255))),
        WallpaperItem("live_emerald", "Emerald Particles", WallpaperType.LIVE, "Nature", intArrayOf(Color.rgb(2, 20, 18), Color.rgb(0, 210, 130), Color.rgb(15, 90, 70))),
        WallpaperItem("live_rose", "Rose Name Glow", WallpaperType.LIVE, "Love", intArrayOf(Color.rgb(40, 5, 25), Color.rgb(255, 87, 140), Color.rgb(150, 30, 80))),
        WallpaperItem("live_islamic", "Midnight Islamic Glow", WallpaperType.LIVE, "Islamic", intArrayOf(Color.rgb(2, 12, 22), Color.rgb(0, 180, 150), Color.rgb(210, 180, 90))),
        WallpaperItem("live_carbon", "Carbon Cyber Motion", WallpaperType.LIVE, "Dark", intArrayOf(Color.rgb(4, 4, 8), Color.rgb(80, 90, 120), Color.rgb(25, 30, 45))),
        WallpaperItem("static_gold", "Black Gold 4K", WallpaperType.STATIC, "Luxury", intArrayOf(Color.rgb(7, 7, 10), Color.rgb(255, 195, 80), Color.rgb(80, 50, 15))),
        WallpaperItem("static_blue", "Deep Blue Lock Screen", WallpaperType.STATIC, "3D", intArrayOf(Color.rgb(6, 10, 45), Color.rgb(0, 190, 255), Color.rgb(60, 30, 190))),
        WallpaperItem("static_green", "Soft Green Depth", WallpaperType.STATIC, "Nature", intArrayOf(Color.rgb(3, 30, 20), Color.rgb(70, 220, 150), Color.rgb(15, 105, 70))),
        WallpaperItem("static_quote", "Minimal Quote Space", WallpaperType.STATIC, "Quotes", intArrayOf(Color.rgb(20, 18, 35), Color.rgb(210, 210, 255), Color.rgb(85, 80, 140))),
        WallpaperItem("static_dark", "Pure Dark Premium", WallpaperType.STATIC, "Dark", intArrayOf(Color.rgb(0, 0, 0), Color.rgb(35, 35, 48), Color.rgb(10, 10, 16))),
        WallpaperItem("static_love", "Love Gradient", WallpaperType.STATIC, "Love", intArrayOf(Color.rgb(55, 8, 35), Color.rgb(255, 95, 145), Color.rgb(160, 40, 85)))
    )

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var selectedType = WallpaperType.LIVE
    private var selectedCategory = "All"
    private val prefs by lazy { getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE) }
    private val favoriteIds: MutableSet<String> by lazy {
        prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildLayout()
        renderHome()
    }

    private fun buildLayout() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(5, 8, 22))
            setPadding(dp(16), dp(18), dp(16), dp(12))
        }

        val title = TextView(this).apply {
            text = "3D Live Wallpapers"
            setTextColor(Color.WHITE)
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Live + Static mobile wallpapers"
            setTextColor(Color.rgb(175, 185, 210))
            textSize = 14f
            setPadding(0, dp(4), 0, dp(14))
        }

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        tabs.addView(tabButton("Live") {
            selectedType = WallpaperType.LIVE
            selectedCategory = "All"
            renderHome()
        })
        tabs.addView(tabButton("Static") {
            selectedType = WallpaperType.STATIC
            selectedCategory = "All"
            renderHome()
        })
        tabs.addView(tabButton("Favorites") {
            renderFavorites()
        })

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(tabs)
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    private fun tabButton(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 14f
        gravity = Gravity.CENTER
        typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(14), dp(10), dp(14), dp(10))
        background = roundedBg(Color.rgb(22, 28, 55), dp(18))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(dp(4), 0, dp(4), dp(14))
        }
    }

    private fun renderHome() {
        val items = wallpapers.filter { it.type == selectedType }
        val categories = listOf("All") + items.map { it.category }.distinct()
        val filtered = if (selectedCategory == "All") items else items.filter { it.category == selectedCategory }
        renderList(title = if (selectedType == WallpaperType.LIVE) "Live Wallpapers" else "Static Wallpapers", categories = categories, items = filtered)
    }

    private fun renderFavorites() {
        val items = wallpapers.filter { favoriteIds.contains(it.id) }
        renderList(title = "Favorites", categories = emptyList(), items = items)
    }

    private fun renderList(title: String, categories: List<String>, items: List<WallpaperItem>) {
        content.removeAllViews()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, dp(24))
        }
        val sectionTitle = TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(10))
        }
        box.addView(sectionTitle)

        if (categories.isNotEmpty()) {
            val chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            categories.forEach { category ->
                chips.addView(categoryChip(category))
            }
            box.addView(chips)
        }

        if (items.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "No wallpapers yet. Add favorites from Live or Static tabs."
                setTextColor(Color.rgb(175, 185, 210))
                textSize = 15f
                setPadding(0, dp(24), 0, 0)
            })
        } else {
            val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            items.chunked(2).forEach { rowItems ->
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                rowItems.forEach { item -> row.addView(card(item)) }
                if (rowItems.size == 1) {
                    row.addView(View(this), LinearLayout.LayoutParams(0, dp(210), 1f))
                }
                grid.addView(row)
            }
            box.addView(grid)
        }

        scroll.addView(box)
        content.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun categoryChip(category: String): TextView = TextView(this).apply {
        text = category
        setTextColor(if (category == selectedCategory) Color.rgb(5, 8, 22) else Color.WHITE)
        textSize = 13f
        gravity = Gravity.CENTER
        typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = roundedBg(if (category == selectedCategory) Color.WHITE else Color.rgb(22, 28, 55), dp(18))
        setOnClickListener {
            selectedCategory = category
            renderHome()
        }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, dp(8), dp(12))
        }
    }

    private fun card(item: WallpaperItem): FrameLayout {
        val frame = FrameLayout(this)
        frame.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, item.colors).apply {
            cornerRadius = dp(22).toFloat()
        }
        frame.setPadding(dp(10), dp(10), dp(10), dp(10))
        frame.setOnClickListener { openPreview(item) }

        val badge = TextView(this).apply {
            text = if (item.type == WallpaperType.LIVE) "LIVE" else "STATIC"
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = roundedBg(Color.argb(130, 0, 0, 0), dp(14))
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        frame.addView(badge, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START))

        val star = TextView(this).apply {
            text = if (favoriteIds.contains(item.id)) "★" else "☆"
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            setOnClickListener {
                toggleFavorite(item)
                renderHome()
            }
        }
        frame.addView(star, FrameLayout.LayoutParams(dp(42), dp(42), Gravity.TOP or Gravity.END))

        val title = TextView(this).apply {
            text = item.title
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(8f, 0f, 2f, Color.argb(160, 0, 0, 0))
        }
        frame.addView(title, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START))

        frame.layoutParams = LinearLayout.LayoutParams(0, dp(210), 1f).apply {
            setMargins(dp(4), dp(4), dp(4), dp(10))
        }
        return frame
    }

    private fun openPreview(item: WallpaperItem) {
        content.removeAllViews()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(6), 0, dp(24))
        }
        box.addView(TextView(this).apply {
            text = item.title
            setTextColor(Color.WHITE)
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(10))
        })
        box.addView(TextView(this).apply {
            text = "${item.category} • ${if (item.type == WallpaperType.LIVE) "Live Wallpaper" else "Static Wallpaper"}"
            setTextColor(Color.rgb(175, 185, 210))
            textSize = 14f
            setPadding(0, 0, 0, dp(14))
        })
        box.addView(PreviewView(this, item), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(460)).apply {
            setMargins(0, 0, 0, dp(16))
        })
        box.addView(actionButton(if (favoriteIds.contains(item.id)) "Remove from Favorites" else "Add to Favorites") {
            toggleFavorite(item)
            openPreview(item)
        })
        if (item.type == WallpaperType.LIVE) {
            box.addView(actionButton("Set as Live Wallpaper") {
                saveLiveStyle(item)
                openLiveWallpaperPicker()
            })
        } else {
            box.addView(actionButton("Set as Static Wallpaper") {
                setStaticWallpaper(item)
            })
        }
        box.addView(actionButton("Back") { renderHome() })
        scroll.addView(box)
        content.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun actionButton(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = roundedBg(Color.rgb(35, 43, 80), dp(18))
        setPadding(dp(14), dp(14), dp(14), dp(14))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, dp(10))
        }
    }

    private fun toggleFavorite(item: WallpaperItem) {
        if (favoriteIds.contains(item.id)) favoriteIds.remove(item.id) else favoriteIds.add(item.id)
        prefs.edit().putStringSet("favorites", favoriteIds).apply()
    }

    private fun saveLiveStyle(item: WallpaperItem) {
        prefs.edit()
            .putInt("live_color_0", item.colors[0])
            .putInt("live_color_1", item.colors[1])
            .putInt("live_color_2", item.colors[2])
            .putString("live_title", item.title)
            .apply()
    }

    private fun openLiveWallpaperPicker() {
        val component = ComponentName(this, MotionLiveWallpaperService::class.java)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }
    }

    private fun setStaticWallpaper(item: WallpaperItem) {
        try {
            val bitmap = createWallpaperBitmap(item)
            WallpaperManager.getInstance(this).setBitmap(
                bitmap,
                null,
                true,
                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            )
            Toast.makeText(this, "Wallpaper applied", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not set wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createWallpaperBitmap(item: WallpaperItem): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), item.colors, null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = Color.argb(70, 255, 255, 255)
        for (i in 0..9) {
            canvas.drawCircle((120 + i * 110).toFloat(), (300 + (i % 4) * 260).toFloat(), (80 + i * 8).toFloat(), paint)
        }
        paint.color = Color.WHITE
        paint.textSize = 64f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.setShadowLayer(18f, 0f, 5f, Color.argb(180, 0, 0, 0))
        canvas.drawText(item.title, 80f, 1540f, paint)
        paint.textSize = 34f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("3D Live Wallpapers", 80f, 1600f, paint)
        return bitmap
    }

    private fun roundedBg(color: Int, radius: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private inner class PreviewView(context: Context, private val item: WallpaperItem) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            paint.shader = LinearGradient(0f, 0f, w, h, item.colors, null, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(RectF(0f, 0f, w, h), dp(28).toFloat(), dp(28).toFloat(), paint)
            paint.shader = null
            paint.color = Color.argb(60, 255, 255, 255)
            val step = min(w, h) / 5f
            for (i in 0..7) {
                canvas.drawCircle(w * 0.2f + i * step * 0.45f, h * 0.2f + (i % 4) * step, step * (0.7f + i * 0.08f), paint)
            }
            paint.color = Color.WHITE
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = dp(24).toFloat()
            paint.setShadowLayer(10f, 0f, 2f, Color.argb(160, 0, 0, 0))
            canvas.drawText(item.title, dp(24).toFloat(), h - dp(64).toFloat(), paint)
            paint.typeface = Typeface.DEFAULT
            paint.textSize = dp(14).toFloat()
            canvas.drawText(if (item.type == WallpaperType.LIVE) "Animated native preview" else "Static wallpaper preview", dp(24).toFloat(), h - dp(36).toFloat(), paint)
        }
    }
}
