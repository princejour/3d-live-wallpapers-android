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
import kotlin.math.cos
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

    private val wallpapers: List<WallpaperItem> = makeWallpapers()
    private lateinit var content: LinearLayout
    private var selectedType = WallpaperType.LIVE
    private var selectedCategory = "All"
    private var showingFavorites = false
    private val prefs by lazy { getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE) }
    private val favoriteIds: MutableSet<String> by lazy { prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(5, 8, 22)
        window.navigationBarColor = Color.rgb(5, 8, 22)
        buildLayout()
        renderHome()
    }

    private fun makeWallpapers(): List<WallpaperItem> {
        val list = mutableListOf<WallpaperItem>()
        fun live(id: String, title: String, cat: String, tags: String, lock: Int, motion: String) {
            list += WallpaperItem(id, title, WallpaperType.LIVE, cat, photo(tags, lock), motion)
        }
        fun stat(id: String, title: String, cat: String, tags: String, lock: Int) {
            list += WallpaperItem(id, title, WallpaperType.STATIC, cat, photo(tags, lock), "static")
        }
        live("live_islamic_01", "Makkah Night Motion", "Islamic", "kaaba,islamic", 1101, "glow")
        live("live_islamic_02", "Golden Mosque Lights", "Islamic", "mosque,architecture", 1102, "glow")
        live("live_islamic_03", "Ramadan Lantern", "Islamic", "ramadan,lantern", 1103, "particles")
        live("live_islamic_04", "Crescent Sky", "Islamic", "crescent,night", 1104, "stars")
        live("live_islamic_05", "Prayer Calm", "Islamic", "prayer,mosque", 1105, "slow_zoom")
        live("live_sports_01", "Football Stadium", "Sports", "football,stadium", 1201, "stadium")
        live("live_sports_02", "Soccer Energy", "Sports", "soccer,ball", 1202, "energy")
        live("live_sports_03", "Champion Arena", "Sports", "sports,arena", 1203, "stadium")
        live("live_sports_04", "Running Power", "Sports", "running,athlete", 1204, "energy")
        live("live_sports_05", "Basketball Lights", "Sports", "basketball,court", 1205, "stadium")
        live("live_luxury_01", "Black Gold Motion", "Luxury", "luxury,gold", 1301, "gold")
        live("live_luxury_02", "Premium Car Shine", "Luxury", "luxury,car", 1302, "shine")
        live("live_luxury_03", "Diamond Glow", "Luxury", "diamond,jewelry", 1303, "shine")
        live("live_luxury_04", "Luxury Hotel Night", "Luxury", "luxury,hotel", 1304, "slow_zoom")
        live("live_luxury_05", "Elegant Watch", "Luxury", "luxury,watch", 1305, "gold")
        live("live_nature_01", "Ocean Waves", "Nature", "ocean,waves", 1401, "wave")
        live("live_nature_02", "Rain Forest", "Nature", "rain,forest", 1402, "rain")
        live("live_nature_03", "Mountain Clouds", "Nature", "mountain,clouds", 1403, "slow_zoom")
        live("live_nature_04", "Waterfall Fresh", "Nature", "waterfall,nature", 1404, "rain")
        live("live_nature_05", "Northern Sky", "Nature", "aurora,sky", 1405, "stars")
        live("live_love_01", "Romantic Roses", "Love", "roses,love", 1501, "hearts")
        live("live_love_02", "Couple Sunset", "Love", "couple,sunset", 1502, "hearts")
        live("live_love_03", "Red Heart Glow", "Love", "heart,romantic", 1503, "hearts")
        live("live_love_04", "Love Lights", "Love", "love,lights", 1504, "glow")
        live("live_love_05", "Wedding Dream", "Love", "wedding,romantic", 1505, "slow_zoom")
        live("live_abstract_01", "Neon City", "3D / Abstract", "neon,city", 1601, "neon")
        live("live_abstract_02", "Galaxy Depth", "3D / Abstract", "galaxy,space", 1602, "stars")
        live("live_abstract_03", "Cyber Light", "3D / Abstract", "cyberpunk,neon", 1603, "neon")
        live("live_abstract_04", "Digital Abstract", "3D / Abstract", "abstract,technology", 1604, "particles")
        live("live_abstract_05", "3D Future", "3D / Abstract", "futuristic,3d", 1605, "shine")
        stat("static_islamic_01", "Grand Mosque", "Islamic", "grand,mosque", 2101)
        stat("static_islamic_02", "Islamic Architecture", "Islamic", "islamic,architecture", 2102)
        stat("static_islamic_03", "Ramadan Night", "Islamic", "ramadan,night", 2103)
        stat("static_islamic_04", "Peaceful Prayer", "Islamic", "muslim,prayer", 2104)
        stat("static_islamic_05", "Crescent Moon", "Islamic", "crescent,moon", 2105)
        stat("static_sports_01", "Football Field", "Sports", "football,field", 2201)
        stat("static_sports_02", "Sports Victory", "Sports", "sports,victory", 2202)
        stat("static_sports_03", "Stadium Crowd", "Sports", "stadium,crowd", 2203)
        stat("static_sports_04", "Athlete Focus", "Sports", "athlete,training", 2204)
        stat("static_sports_05", "Basketball Court", "Sports", "basketball,arena", 2205)
        stat("static_luxury_01", "Luxury Interior", "Luxury", "luxury,interior", 2301)
        stat("static_luxury_02", "Gold Details", "Luxury", "gold,luxury", 2302)
        stat("static_luxury_03", "Premium Car", "Luxury", "premium,car", 2303)
        stat("static_luxury_04", "Diamond Style", "Luxury", "diamond,luxury", 2304)
        stat("static_luxury_05", "Marble Premium", "Luxury", "marble,luxury", 2305)
        stat("static_nature_01", "Blue Ocean", "Nature", "blue,ocean", 2401)
        stat("static_nature_02", "Green Forest", "Nature", "green,forest", 2402)
        stat("static_nature_03", "Snow Mountain", "Nature", "snow,mountain", 2403)
        stat("static_nature_04", "Sunset Lake", "Nature", "sunset,lake", 2404)
        stat("static_nature_05", "Waterfall View", "Nature", "waterfall,landscape", 2405)
        stat("static_love_01", "Red Roses", "Love", "red,roses", 2501)
        stat("static_love_02", "Romantic Couple", "Love", "romantic,couple", 2502)
        stat("static_love_03", "Heart Lights", "Love", "heart,lights", 2503)
        stat("static_love_04", "Love Sunset", "Love", "love,sunset", 2504)
        stat("static_love_05", "Wedding Rings", "Love", "wedding,rings", 2505)
        stat("static_abstract_01", "Neon Abstract", "3D / Abstract", "neon,abstract", 2601)
        stat("static_abstract_02", "Space Galaxy", "3D / Abstract", "space,galaxy", 2602)
        stat("static_abstract_03", "Tech Pattern", "3D / Abstract", "technology,abstract", 2603)
        stat("static_abstract_04", "Digital Light", "3D / Abstract", "digital,light", 2604)
        stat("static_abstract_05", "Future City", "3D / Abstract", "future,city", 2605)
        return list
    }

    private fun buildLayout() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(5, 8, 22))
            setPadding(dp(10), safeTop(), dp(10), safeBottom())
        }
        root.addView(TextView(this).apply {
            text = "3D Live Wallpapers"
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Visible motion • Real photos"
            setTextColor(Color.rgb(165, 174, 200))
            textSize = 11f
            setPadding(0, dp(1), 0, dp(7))
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
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(22, 28, 55), dp(14))
        setPadding(dp(6), dp(7), dp(6), dp(7))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(3), 0, dp(3), dp(8)) }
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
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(16)) }
        box.addView(TextView(this).apply { text = title; setTextColor(Color.WHITE); textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, 0, 0, dp(6)) })
        if (showCategories) box.addView(categoryBar())
        if (items.isEmpty()) {
            box.addView(TextView(this).apply { text = "No favorites yet."; setTextColor(Color.rgb(170, 180, 205)); textSize = 13f; setPadding(0, dp(12), 0, 0) })
        } else {
            items.chunked(2).forEach { rowItems ->
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                rowItems.forEach { row.addView(card(it)) }
                if (rowItems.size == 1) row.addView(View(this), LinearLayout.LayoutParams(0, dp(146), 1f))
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
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(if (selected) Color.WHITE else Color.rgb(22, 28, 55), dp(14))
        setPadding(dp(9), dp(6), dp(9), dp(6))
        setOnClickListener { selectedCategory = category; showingFavorites = false; renderHome() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, dp(6), dp(8)) }
    }

    private fun card(item: WallpaperItem): FrameLayout {
        val frame = FrameLayout(this).apply {
            background = rounded(Color.rgb(10, 14, 30), dp(15))
            setPadding(dp(2), dp(2), dp(2), dp(2))
            setOnClickListener { openPreview(item) }
        }
        frame.addView(RealPhotoView(this, item.imageUrl, item.motion, item.type == WallpaperType.LIVE), FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        frame.addView(TextView(this).apply {
            text = if (item.type == WallpaperType.LIVE) "LIVE" else "STATIC"
            setTextColor(Color.WHITE); textSize = 8f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            background = rounded(Color.argb(170, 0, 0, 0), dp(8)); setPadding(dp(6), dp(2), dp(6), dp(2))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply { setMargins(dp(5), dp(5), 0, 0) })
        frame.addView(TextView(this).apply {
            text = if (favoriteIds.contains(item.id)) "★" else "☆"
            setTextColor(Color.WHITE); textSize = 20f; gravity = Gravity.CENTER; setShadowLayer(8f, 0f, 2f, Color.BLACK)
            setOnClickListener { toggleFavorite(item); if (showingFavorites) renderFavorites() else renderHome() }
        }, FrameLayout.LayoutParams(dp(34), dp(34), Gravity.TOP or Gravity.END))
        frame.addView(TextView(this).apply {
            text = item.title
            setTextColor(Color.WHITE); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; setShadowLayer(10f, 0f, 3f, Color.BLACK); setPadding(dp(6), 0, dp(6), dp(6))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START))
        frame.layoutParams = LinearLayout.LayoutParams(0, dp(146), 1f).apply { setMargins(dp(3), dp(3), dp(3), dp(6)) }
        return frame
    }

    private fun openPreview(item: WallpaperItem) {
        content.removeAllViews()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(16)) }
        box.addView(TextView(this).apply { text = item.title; setTextColor(Color.WHITE); textSize = 19f; typeface = Typeface.DEFAULT_BOLD })
        box.addView(TextView(this).apply { text = "${item.category} • ${if (item.type == WallpaperType.LIVE) "animated" else "static"}"; setTextColor(Color.rgb(170, 180, 205)); textSize = 12f; setPadding(0, dp(2), 0, dp(7)) })
        box.addView(RealPhotoView(this, item.imageUrl, item.motion, item.type == WallpaperType.LIVE), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(280)).apply { setMargins(0, 0, 0, dp(9)) })
        box.addView(action(if (favoriteIds.contains(item.id)) "Remove from Favorites" else "Add to Favorites") { toggleFavorite(item); openPreview(item) })
        if (item.type == WallpaperType.LIVE) box.addView(action("Set as Live Wallpaper") { saveLive(item); openLiveWallpaperPicker() }) else box.addView(action("Set as Static Wallpaper") { setStatic(item) })
        box.addView(action("Back") { if (showingFavorites) renderFavorites() else renderHome() })
        scroll.addView(box)
        content.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun action(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label; setTextColor(Color.WHITE); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
        background = rounded(Color.rgb(35, 43, 80), dp(14)); setPadding(dp(10), dp(9), dp(10), dp(9)); setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, dp(7)) }
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
            if (bitmap == null) Toast.makeText(this, "Image download failed", Toast.LENGTH_LONG).show()
            else try {
                WallpaperManager.getInstance(this).setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                Toast.makeText(this, "Static wallpaper applied", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) { Toast.makeText(this, "Could not set wallpaper", Toast.LENGTH_LONG).show() }
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun safeTop(): Int = systemDimen("status_bar_height") + dp(8)
    private fun safeBottom(): Int = max(dp(8), systemDimen("navigation_bar_height") / 2)
    private fun systemDimen(name: String): Int {
        val id = resources.getIdentifier(name, "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }
}

class RealPhotoView(context: Context, private val imageUrl: String, private val motion: String, private val animate: Boolean) : View(context) {
    private var bitmap: Bitmap? = null
    init {
        setBackgroundColor(Color.rgb(10, 14, 30))
        RemoteBitmapStore.load(imageUrl) { loaded -> bitmap = loaded; invalidate() }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        RealWallpaperRenderer.draw(canvas, bitmap, motion, animate, true)
        if (animate) postInvalidateOnAnimation()
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
            } catch (_: Exception) { null }
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
        if (bitmap == null) drawLoading(canvas, w, h, t) else drawPhoto(canvas, bitmap, w, h, t, animate)
        if (animate && overlay) {
            drawMovingLightBand(canvas, w, h, t)
            when (motion) {
                "rain" -> drawRain(canvas, w, h, t)
                "hearts" -> drawHearts(canvas, w, h, t)
                "stadium", "energy" -> drawStadiumLights(canvas, w, h, t)
                "wave" -> drawWaves(canvas, w, h, t)
                else -> drawParticles(canvas, w, h, t, Color.WHITE, 50)
            }
            drawLiveBadge(canvas, w, h, t)
        }
        drawVignette(canvas, w, h)
    }

    private fun drawPhoto(canvas: Canvas, bitmap: Bitmap, w: Float, h: Float, t: Float, animate: Boolean) {
        val baseScale = max(w / bitmap.width, h / bitmap.height)
        val pulse = if (animate) 1.24f + 0.12f * sin(t * 1.2f) else 1f
        val sw = bitmap.width * baseScale * pulse
        val sh = bitmap.height * baseScale * pulse
        val dx = if (animate) sin(t * 1.0f) * w * 0.11f else 0f
        val dy = if (animate) cos(t * 0.8f) * h * 0.08f else 0f
        canvas.drawBitmap(bitmap, null, RectF((w - sw) / 2f + dx, (h - sh) / 2f + dy, (w + sw) / 2f + dx, (h + sh) / 2f + dy), paint)
    }

    private fun drawLoading(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.shader = LinearGradient(0f, 0f, w, h, intArrayOf(Color.rgb(15, 20, 42), Color.rgb(45, 58, 95), Color.rgb(5, 8, 22)), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        paint.color = Color.WHITE
        paint.textSize = min(w, h) * 0.052f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Loading motion...", w * 0.1f, h * 0.5f + sin(t * 4f) * 16f, paint)
    }

    private fun drawMovingLightBand(canvas: Canvas, w: Float, h: Float, t: Float) {
        val x = ((sin(t * 1.6f) + 1f) / 2f) * (w + w * 0.9f) - w * 0.45f
        paint.shader = LinearGradient(x - w * 0.25f, 0f, x + w * 0.25f, h, intArrayOf(Color.TRANSPARENT, Color.argb(150, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    private fun drawParticles(canvas: Canvas, w: Float, h: Float, t: Float, color: Int, count: Int) {
        paint.color = color
        for (i in 0..count) {
            paint.alpha = 95 + (i % 5) * 30
            val x = (w * (((i * 37) % 100) / 100f) + sin(t * 1.6f + i) * w * 0.11f) % w
            val y = (h * (((i * 53) % 100) / 100f) + t * (52 + i % 6 * 12)) % h
            canvas.drawCircle(x, y, 3f + i % 6, paint)
        }
        paint.alpha = 255
    }

    private fun drawRain(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(190, 210, 235, 255)
        paint.strokeWidth = 3.6f
        for (i in 0..82) {
            val x = w * (((i * 29) % 100) / 100f)
            val y = (h * (((i * 47) % 100) / 100f) + t * (270 + i % 8 * 25)) % h
            canvas.drawLine(x, y, x - 20f, y + 60f, paint)
        }
    }

    private fun drawHearts(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(155, 255, 210, 225)
        for (i in 0..18) {
            val x = w * (0.08f + i * 0.055f) + sin(t + i) * 20f
            val y = h * 0.95f - ((t * 110 + i * 71) % (h * 0.85f))
            drawHeart(canvas, x, y, 9f + i % 8)
        }
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float, s: Float) {
        val path = Path()
        path.moveTo(x, y + s)
        path.cubicTo(x - 2f * s, y - .5f * s, x - s, y - 2f * s, x, y - s)
        path.cubicTo(x + s, y - 2f * s, x + 2f * s, y - .5f * s, x, y + s)
        canvas.drawPath(path, paint)
    }

    private fun drawStadiumLights(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.color = Color.argb(165, 255, 255, 255)
        paint.strokeWidth = 4.8f
        for (i in 0..8) canvas.drawLine(w * (0.05f + i * 0.12f), 0f, w * (0.5f + sin(t * 1.8f + i) * 0.45f), h * 0.72f, paint)
    }

    private fun drawWaves(canvas: Canvas, w: Float, h: Float, t: Float) {
        for (layer in 0..3) {
            paint.color = Color.argb(100 + layer * 22, 110, 220, 255)
            val path = Path()
            val base = h * (0.58f + layer * 0.08f)
            path.moveTo(0f, h)
            path.lineTo(0f, base)
            var x = 0f
            while (x <= w) {
                path.lineTo(x, base + sin(x / w * 8f + t * (2.2f + layer * 0.3f)) * (26f + layer * 9f))
                x += 18f
            }
            path.lineTo(w, h)
            path.close()
            canvas.drawPath(path, paint)
        }
    }

    private fun drawLiveBadge(canvas: Canvas, w: Float, h: Float, t: Float) {
        val pulse = ((sin(t * 6f) + 1f) / 2f)
        paint.color = Color.argb((145 + pulse * 100).toInt(), 255, 255, 255)
        canvas.drawCircle(w * 0.12f + pulse * 20f, h * 0.13f, 7f + pulse * 5f, paint)
    }

    private fun drawVignette(canvas: Canvas, w: Float, h: Float) {
        paint.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(Color.argb(100, 0, 0, 0), Color.TRANSPARENT, Color.argb(165, 0, 0, 0)), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        paint.alpha = 255
    }
}
