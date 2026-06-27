package com.walhero.wallpapers

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class MainActivity : Activity() {

    enum class WallpaperType { LIVE, STATIC }

    data class WallpaperItem(
        val id: String,
        val title: String,
        val type: WallpaperType,
        val category: String,
        val imageUrl: String,
        val motion: String
    )

    private val categories = listOf("All", "Islamic", "Sports", "Luxury", "Nature", "Love", "3D / Abstract")

    private fun photo(tags: String, lock: Int): String = "https://loremflickr.com/1080/1920/$tags?lock=$lock"

    private val wallpapers = listOf(
        WallpaperItem("live_islamic_01", "Makkah Night Motion", WallpaperType.LIVE, "Islamic", photo("kaaba,islamic", 1101), "glow"),
        WallpaperItem("live_islamic_02", "Golden Mosque Lights", WallpaperType.LIVE, "Islamic", photo("mosque,architecture", 1102), "glow"),
        WallpaperItem("live_islamic_03", "Ramadan Lantern", WallpaperType.LIVE, "Islamic", photo("ramadan,lantern", 1103), "particles"),
        WallpaperItem("live_islamic_04", "Crescent Sky", WallpaperType.LIVE, "Islamic", photo("crescent,night", 1104), "stars"),
        WallpaperItem("live_islamic_05", "Prayer Calm", WallpaperType.LIVE, "Islamic", photo("prayer,mosque", 1105), "slow_zoom"),

        WallpaperItem("live_sports_01", "Football Stadium", WallpaperType.LIVE, "Sports", photo("football,stadium", 1201), "stadium"),
        WallpaperItem("live_sports_02", "Soccer Energy", WallpaperType.LIVE, "Sports", photo("soccer,ball", 1202), "energy"),
        WallpaperItem("live_sports_03", "Champion Arena", WallpaperType.LIVE, "Sports", photo("sports,arena", 1203), "stadium"),
        WallpaperItem("live_sports_04", "Running Power", WallpaperType.LIVE, "Sports", photo("running,athlete", 1204), "energy"),
        WallpaperItem("live_sports_05", "Basketball Lights", WallpaperType.LIVE, "Sports", photo("basketball,court", 1205), "stadium"),

        WallpaperItem("live_luxury_01", "Black Gold Motion", WallpaperType.LIVE, "Luxury", photo("luxury,gold", 1301), "gold"),
        WallpaperItem("live_luxury_02", "Premium Car Shine", WallpaperType.LIVE, "Luxury", photo("luxury,car", 1302), "shine"),
        WallpaperItem("live_luxury_03", "Diamond Glow", WallpaperType.LIVE, "Luxury", photo("diamond,jewelry", 1303), "shine"),
        WallpaperItem("live_luxury_04", "Luxury Hotel Night", WallpaperType.LIVE, "Luxury", photo("luxury,hotel", 1304), "slow_zoom"),
        WallpaperItem("live_luxury_05", "Elegant Watch", WallpaperType.LIVE, "Luxury", photo("luxury,watch", 1305), "gold"),

        WallpaperItem("live_nature_01", "Ocean Waves", WallpaperType.LIVE, "Nature", photo("ocean,waves", 1401), "wave"),
        WallpaperItem("live_nature_02", "Rain Forest", WallpaperType.LIVE, "Nature", photo("rain,forest", 1402), "rain"),
        WallpaperItem("live_nature_03", "Mountain Clouds", WallpaperType.LIVE, "Nature", photo("mountain,clouds", 1403), "slow_zoom"),
        WallpaperItem("live_nature_04", "Waterfall Fresh", WallpaperType.LIVE, "Nature", photo("waterfall,nature", 1404), "rain"),
        WallpaperItem("live_nature_05", "Northern Sky", WallpaperType.LIVE, "Nature", photo("aurora,sky", 1405), "stars"),

        WallpaperItem("live_love_01", "Romantic Roses", WallpaperType.LIVE, "Love", photo("roses,love", 1501), "hearts"),
        WallpaperItem("live_love_02", "Couple Sunset", WallpaperType.LIVE, "Love", photo("couple,sunset", 1502), "hearts"),
        WallpaperItem("live_love_03", "Red Heart Glow", WallpaperType.LIVE, "Love", photo("heart,romantic", 1503), "hearts"),
        WallpaperItem("live_love_04", "Love Lights", WallpaperType.LIVE, "Love", photo("love,lights", 1504), "glow"),
        WallpaperItem("live_love_05", "Wedding Dream", WallpaperType.LIVE, "Love", photo("wedding,romantic", 1505), "slow_zoom"),

        WallpaperItem("live_abstract_01", "Neon City", WallpaperType.LIVE, "3D / Abstract", photo("neon,city", 1601), "neon"),
        WallpaperItem("live_abstract_02", "Galaxy Depth", WallpaperType.LIVE, "3D / Abstract", photo("galaxy,space", 1602), "stars"),
        WallpaperItem("live_abstract_03", "Cyber Light", WallpaperType.LIVE, "3D / Abstract", photo("cyberpunk,neon", 1603), "neon"),
        WallpaperItem("live_abstract_04", "Digital Abstract", WallpaperType.LIVE, "3D / Abstract", photo("abstract,technology", 1604), "particles"),
        WallpaperItem("live_abstract_05", "3D Future", WallpaperType.LIVE, "3D / Abstract", photo("futuristic,3d", 1605), "shine"),

        WallpaperItem("static_islamic_01", "Grand Mosque", WallpaperType.STATIC, "Islamic", photo("grand,mosque", 2101), "static"),
        WallpaperItem("static_islamic_02", "Islamic Architecture", WallpaperType.STATIC, "Islamic", photo("islamic,architecture", 2102), "static"),
        WallpaperItem("static_islamic_03", "Ramadan Night", WallpaperType.STATIC, "Islamic", photo("ramadan,night", 2103), "static"),
        WallpaperItem("static_islamic_04", "Peaceful Prayer", WallpaperType.STATIC, "Islamic", photo("muslim,prayer", 2104), "static"),
        WallpaperItem("static_islamic_05", "Crescent Moon", WallpaperType.STATIC, "Islamic", photo("crescent,moon", 2105), "static"),

        WallpaperItem("static_sports_01", "Football Field", WallpaperType.STATIC, "Sports", photo("football,field", 2201), "static"),
        WallpaperItem("static_sports_02", "Sports Victory", WallpaperType.STATIC, "Sports", photo("sports,victory", 2202), "static"),
        WallpaperItem("static_sports_03", "Stadium Crowd", WallpaperType.STATIC, "Sports", photo("stadium,crowd", 2203), "static"),
        WallpaperItem("static_sports_04", "Athlete Focus", WallpaperType.STATIC, "Sports", photo("athlete,training", 2204), "static"),
        WallpaperItem("static_sports_05", "Basketball Court", WallpaperType.STATIC, "Sports", photo("basketball,arena", 2205), "static"),

        WallpaperItem("static_luxury_01", "Luxury Interior", WallpaperType.STATIC, "Luxury", photo("luxury,interior", 2301), "static"),
        WallpaperItem("static_luxury_02", "Gold Details", WallpaperType.STATIC, "Luxury", photo("gold,luxury", 2302), "static"),
        WallpaperItem("static_luxury_03", "Premium Car", WallpaperType.STATIC, "Luxury", photo("premium,car", 2303), "static"),
        WallpaperItem("static_luxury_04", "Diamond Style", WallpaperType.STATIC, "Luxury", photo("diamond,luxury", 2304), "static"),
        WallpaperItem("static_luxury_05", "Marble Premium", WallpaperType.STATIC, "Luxury", photo("marble,luxury", 2305), "static"),

        WallpaperItem("static_nature_01", "Blue Ocean", WallpaperType.STATIC, "Nature", photo("blue,ocean", 2401), "static"),
        WallpaperItem("static_nature_02", "Green Forest", WallpaperType.STATIC, "Nature", photo("green,forest", 2402), "static"),
        WallpaperItem("static_nature_03", "Snow Mountain", WallpaperType.STATIC, "Nature", photo("snow,mountain", 2403), "static"),
        WallpaperItem("static_nature_04", "Sunset Lake", WallpaperType.STATIC, "Nature", photo("sunset,lake", 2404), "static"),
        WallpaperItem("static_nature_05", "Waterfall View", WallpaperType.STATIC, "Nature", photo("waterfall,landscape", 2405), "static"),

        WallpaperItem("static_love_01", "Red Roses", WallpaperType.STATIC, "Love", photo("red,roses", 2501), "static"),
        WallpaperItem("static_love_02", "Romantic Couple", WallpaperType.STATIC, "Love", photo("romantic,couple", 2502), "static"),
        WallpaperItem("static_love_03", "Heart Lights", WallpaperType.STATIC, "Love", photo("heart,lights", 2503), "static"),
        WallpaperItem("static_love_04", "Love Sunset", WallpaperType.STATIC, "Love", photo("love,sunset", 2504), "static"),
        WallpaperItem("static_love_05", "Wedding Rings", WallpaperType.STATIC, "Love", photo("wedding,rings", 2505), "static"),

        WallpaperItem("static_abstract_01", "Neon Abstract", WallpaperType.STATIC, "3D / Abstract", photo("neon,abstract", 2601), "static"),
        WallpaperItem("static_abstract_02", "Space Galaxy", WallpaperType.STATIC, "3D / Abstract", photo("space,galaxy", 2602), "static"),
        WallpaperItem("static_abstract_03", "Tech Pattern", WallpaperType.STATIC, "3D / Abstract", photo("technology,abstract", 2603), "static"),
        WallpaperItem("static_abstract_04", "Digital Light", WallpaperType.STATIC, "3D / Abstract", photo("digital,light", 2604), "static"),
        WallpaperItem("static_abstract_05", "Future City", WallpaperType.STATIC, "3D / Abstract", photo("future,city", 2605), "static")
    )

    private lateinit var content: LinearLayout
    private var selectedType = WallpaperType.LIVE
    private var selectedCategory = "All"
    private var showingFavorites = false
    private val prefs by lazy { getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE) }
    private val favoriteIds: MutableSet<String> by lazy { prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildLayout()
        renderHome()
    }

    private fun buildLayout() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(5, 8, 22))
            setPadding(dp(12), dp(12), dp(12), dp(8))
        }
        root.addView(TextView(this).apply {
            text = "3D Live Wallpapers"
            setTextColor(Color.WHITE)
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Real photos • Real motion • APK + AAB"
            setTextColor(Color.rgb(165, 174, 200))
            textSize = 12f
            setPadding(0, dp(3), 0, dp(10))
        })
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tabs.addView(tabButton("Live") { showingFavorites = false; selectedType = WallpaperType.LIVE; selectedCategory = "All"; renderHome() })
        tabs.addView(tabButton("Static") { showingFavorites = false; selectedType = WallpaperType.STATIC; selectedCategory = "All"; renderHome() })
        tabs.addView(tabButton("Favorites") { showingFavorites = true; renderFavorites() })
        root.addView(tabs)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    private fun tabButton(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(22, 28, 55), dp(16))
        setPadding(dp(8), dp(9), dp(8), dp(9))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(3), 0, dp(3), dp(10)) }
    }

    private fun renderHome() {
        val items = wallpapers.filter { it.type == selectedType && (selectedCategory == "All" || it.category == selectedCategory) }
        renderList(if (selectedType == WallpaperType.LIVE) "Live Wallpapers" else "Static Wallpapers", items, true)
    }

    private fun renderFavorites() {
        renderList("Favorites", wallpapers.filter { favoriteIds.contains(it.id) }, false)
    }

    private fun renderList(title: String, items: List<WallpaperItem>, showCategories: Boolean) {
        content.removeAllViews()
        val scroll = ScrollView(this).apply { isFillViewport = false }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(42)) }
        box.addView(TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(8))
        })
        if (showCategories) box.addView(categoryBar())
        if (items.isEmpty()) {
            box.addView(TextView(this).apply { text = "No favorites yet."; setTextColor(Color.rgb(170, 180, 205)); textSize = 14f; setPadding(0, dp(20), 0, 0) })
        } else {
            items.chunked(2).forEach { rowItems ->
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                rowItems.forEach { row.addView(card(it)) }
                if (rowItems.size == 1) row.addView(View(this), LinearLayout.LayoutParams(0, dp(180), 1f))
                box.addView(row)
            }
        }
        scroll.addView(box)
        content.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun categoryBar(): HorizontalScrollView {
        val scroller = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        categories.forEach { row.addView(categoryChip(it)) }
        scroller.addView(row)
        return scroller
    }

    private fun categoryChip(category: String): TextView = TextView(this).apply {
        text = category
        val selected = category == selectedCategory
        setTextColor(if (selected) Color.rgb(5, 8, 22) else Color.WHITE)
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(if (selected) Color.WHITE else Color.rgb(22, 28, 55), dp(16))
        setPadding(dp(11), dp(8), dp(11), dp(8))
        setOnClickListener { selectedCategory = category; showingFavorites = false; renderHome() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, dp(7), dp(11)) }
    }

    private fun card(item: WallpaperItem): FrameLayout {
        val frame = FrameLayout(this).apply {
            background = rounded(Color.rgb(10, 14, 30), dp(18))
            setPadding(dp(3), dp(3), dp(3), dp(3))
            setOnClickListener { openPreview(item) }
        }
        frame.addView(RealPhotoView(this, item.imageUrl, item.motion, item.type == WallpaperType.LIVE), FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        frame.addView(TextView(this).apply {
            text = if (item.type == WallpaperType.LIVE) "LIVE" else "STATIC"
            setTextColor(Color.WHITE)
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(Color.argb(165, 0, 0, 0), dp(10))
            setPadding(dp(7), dp(3), dp(7), dp(3))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply { setMargins(dp(6), dp(6), 0, 0) })
        frame.addView(TextView(this).apply {
            text = if (favoriteIds.contains(item.id)) "★" else "☆"
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            setShadowLayer(8f, 0f, 2f, Color.BLACK)
            setOnClickListener {
                toggleFavorite(item)
                if (showingFavorites) renderFavorites() else renderHome()
            }
        }, FrameLayout.LayoutParams(dp(40), dp(40), Gravity.TOP or Gravity.END))
        frame.addView(TextView(this).apply {
            text = item.title
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(10f, 0f, 3f, Color.BLACK)
            setPadding(dp(7), 0, dp(7), dp(7))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START))
        frame.layoutParams = LinearLayout.LayoutParams(0, dp(180), 1f).apply { setMargins(dp(3), dp(3), dp(3), dp(8)) }
        return frame
    }

    private fun openPreview(item: WallpaperItem) {
        content.removeAllViews()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(42)) }
        box.addView(TextView(this).apply { text = item.title; setTextColor(Color.WHITE); textSize = 22f; typeface = Typeface.DEFAULT_BOLD })
        box.addView(TextView(this).apply { text = "${item.category} • ${if (item.type == WallpaperType.LIVE) "Real animated live wallpaper" else "Real static wallpaper"}"; setTextColor(Color.rgb(170, 180, 205)); textSize = 13f; setPadding(0, dp(3), 0, dp(10)) })
        box.addView(RealPhotoView(this, item.imageUrl, item.motion, item.type == WallpaperType.LIVE), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(420)).apply { setMargins(0, 0, 0, dp(12)) })
        box.addView(action(if (favoriteIds.contains(item.id)) "Remove from Favorites" else "Add to Favorites") { toggleFavorite(item); openPreview(item) })
        if (item.type == WallpaperType.LIVE) box.addView(action("Set as Live Wallpaper") { saveLive(item); openLiveWallpaperPicker() }) else box.addView(action("Set as Static Wallpaper") { setStatic(item) })
        box.addView(action("Back") { if (showingFavorites) renderFavorites() else renderHome() })
        scroll.addView(box)
        content.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun action(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(35, 43, 80), dp(16))
        setPadding(dp(12), dp(12), dp(12), dp(12))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, dp(9)) }
    }

    private fun toggleFavorite(item: WallpaperItem) {
        if (favoriteIds.contains(item.id)) favoriteIds.remove(item.id) else favoriteIds.add(item.id)
        prefs.edit().putStringSet("favorites", favoriteIds).apply()
    }

    private fun saveLive(item: WallpaperItem) {
        prefs.edit().putString("live_image_url", item.imageUrl).putString("live_title", item.title).putString("live_motion", item.motion).apply()
    }

    private fun openLiveWallpaperPicker() {
        val component = ComponentName(this, MotionLiveWallpaperService::class.java)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply { putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component) }
        try { startActivity(intent) } catch (_: Exception) { startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)) }
    }

    private fun setStatic(item: WallpaperItem) {
        Toast.makeText(this, "Downloading wallpaper...", Toast.LENGTH_SHORT).show()
        RemoteBitmapStore.load(item.imageUrl) { bitmap ->
            if (bitmap == null) {
                Toast.makeText(this, "Image download failed", Toast.LENGTH_LONG).show()
            } else {
                try {
                    WallpaperManager.getInstance(this).setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    Toast.makeText(this, "Static wallpaper applied", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(this, "Could not set wallpaper", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

class RealPhotoView(context: Context, private val imageUrl: String, private val motion: String, private val animate: Boolean) : View(context) {
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    init {
        setBackgroundColor(Color.rgb(10, 14, 30))
        RemoteBitmapStore.load(imageUrl) { loaded -> bitmap = loaded; invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        RealWallpaperRenderer.draw(canvas, bitmap, motion, animate, true)
        if (animate) postInvalidateDelayed(33L)
    }
}

object RemoteBitmapStore {
    private val cache = ConcurrentHashMap<String, Bitmap>()
    private val executor = Executors.newFixedThreadPool(4)
    private val main = Handler(Looper.getMainLooper())

    fun load(url: String, callback: (Bitmap?) -> Unit) {
        cache[url]?.let { callback(it); return }
        executor.execute {
            val bitmap = try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 12000
                connection.readTimeout = 16000
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connect()
                BitmapFactory.decodeStream(connection.inputStream)
            } catch (_: Exception) {
                null
            }
            if (bitmap != null) cache[url] = bitmap
            main.post { callback(bitmap) }
        }
    }
}

object RealWallpaperRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun draw(canvas: Canvas, bitmap: Bitmap?, motion: String, animate: Boolean, overlay: Boolean) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val t = if (animate) System.currentTimeMillis() / 1000f else 0f
        canvas.drawColor(Color.rgb(7, 10, 24))
        if (bitmap == null) {
            drawLoading(canvas, w, h, t)
        } else {
            drawPhoto(canvas, bitmap, w, h, t, animate)
            if (overlay && animate) drawMotionOverlay(canvas, w, h, t, motion)
        }
        drawVignette(canvas, w, h)
    }

    private fun drawPhoto(canvas: Canvas, bitmap: Bitmap, w: Float, h: Float, t: Float, animate: Boolean) {
        val baseScale = max(w / bitmap.width, h / bitmap.height)
        val motionScale = if (animate) 1.08f + 0.025f * sin(t * 0.35f) else 1f
        val scaledW = bitmap.width * baseScale * motionScale
        val scaledH = bitmap.height * baseScale * motionScale
        val dx = if (animate) sin(t * 0.18f) * (scaledW - w) * 0.22f else 0f
        val dy = if (animate) sin(t * 0.13f) * (scaledH - h) * 0.18f else 0f
        val left = (w - scaledW) / 2f + dx
        val top = (h - scaledH) / 2f + dy
        canvas.drawBitmap(bitmap, null, RectF(left, top, left + scaledW, top + scaledH), paint)
    }

    private fun drawMotionOverlay(canvas: Canvas, w: Float, h: Float, t: Float, motion: String) {
        when (motion) {
            "rain" -> drawRain(canvas, w, h, t)
            "hearts" -> drawHearts(canvas, w, h, t)
            "stars" -> drawParticles(canvas, w, h, t, Color.WHITE)
            "stadium", "energy" -> drawLightSweeps(canvas, w, h, t)
            "gold", "shine", "glow", "neon" -> drawGlow(canvas, w, h, t)
            "wave" -> drawWaveTint(canvas, w, h, t)
            else -> drawParticles(canvas, w, h, t, Color.WHITE)
        }
    }

    private fun drawLoading(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.shader = LinearGradient(0f, 0f, w, h, intArrayOf(Color.rgb(15, 20, 42), Color.rgb(45, 58, 95), Color.rgb(5, 8, 22)), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        paint.color = Color.argb(130, 255, 255, 255)
        paint.textSize = min(w, h) * 0.055f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Loading real photo...", w * 0.11f, h * 0.5f + sin(t * 3f) * 10f, paint)
    }

    private fun drawVignette(canvas: Canvas, w: Float, h: Float) {
        paint.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(Color.argb(95, 0, 0, 0), Color.TRANSPARENT, Color.argb(145, 0, 0, 0)), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    private fun drawGlow(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(90, 255, 235, 160)
        for (i in 0..4) canvas.drawCircle(w * (0.18f + i * 0.2f), h * (0.22f + 0.05f * sin(t + i)), w * (0.12f + i * 0.01f), paint)
    }

    private fun drawParticles(canvas: Canvas, w: Float, h: Float, t: Float, color: Int) {
        paint.color = color
        for (i in 0..34) {
            paint.alpha = 60 + (i % 5) * 24
            val x = w * (((i * 37) % 100) / 100f)
            val y = (h * (((i * 53) % 100) / 100f) + t * (16 + i % 6 * 4)) % h
            canvas.drawCircle(x, y, 1.6f + i % 4, paint)
        }
        paint.alpha = 255
    }

    private fun drawRain(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(135, 210, 235, 255)
        paint.strokeWidth = 2.4f
        for (i in 0..54) {
            val x = w * (((i * 29) % 100) / 100f)
            val y = (h * (((i * 47) % 100) / 100f) + t * (130 + i % 8 * 12)) % h
            canvas.drawLine(x, y, x - 14f, y + 38f, paint)
        }
    }

    private fun drawHearts(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(95, 255, 220, 230)
        for (i in 0..11) {
            val x = w * (0.12f + i * 0.075f)
            val y = h * 0.9f - ((t * 42 + i * 91) % (h * 0.72f))
            drawHeart(canvas, x, y, 8f + i % 6)
        }
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float, s: Float) {
        val path = Path()
        path.moveTo(x, y + s)
        path.cubicTo(x - 2f * s, y - .5f * s, x - s, y - 2f * s, x, y - s)
        path.cubicTo(x + s, y - 2f * s, x + 2f * s, y - .5f * s, x, y + s)
        canvas.drawPath(path, paint)
    }

    private fun drawLightSweeps(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(100, 255, 255, 255)
        paint.strokeWidth = 3f
        for (i in 0..5) {
            val startX = w * (0.12f + i * 0.16f)
            val endX = w * (0.5f + sin(t + i) * 0.3f)
            canvas.drawLine(startX, 0f, endX, h * 0.7f, paint)
        }
    }

    private fun drawWaveTint(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(70, 120, 220, 255)
        val path = Path()
        val base = h * 0.65f
        path.moveTo(0f, h)
        path.lineTo(0f, base)
        var x = 0f
        while (x <= w) {
            path.lineTo(x, base + sin(x / w * 7f + t) * 25f)
            x += 22f
        }
        path.lineTo(w, h)
        path.close()
        canvas.drawPath(path, paint)
    }
}
