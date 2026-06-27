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
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MainActivity : Activity() {

    private enum class WallpaperType { LIVE, STATIC }

    private data class WallpaperItem(
        val id: String,
        val title: String,
        val type: WallpaperType,
        val category: String,
        val design: String
    )

    private val categories = listOf("All", "Islamic", "Sports", "Luxury", "Nature", "Love", "3D / Abstract")

    private val wallpapers = listOf(
        WallpaperItem("live_islamic_orbit", "Islamic Moon Orbit", WallpaperType.LIVE, "Islamic", "islamic_orbit"),
        WallpaperItem("live_islamic_lantern", "Ramadan Lantern Glow", WallpaperType.LIVE, "Islamic", "islamic_lantern"),
        WallpaperItem("live_sports_ball", "Football Energy", WallpaperType.LIVE, "Sports", "football_energy"),
        WallpaperItem("live_sports_stadium", "Stadium Lights", WallpaperType.LIVE, "Sports", "stadium_lights"),
        WallpaperItem("live_luxury_gold", "Luxury Gold Motion", WallpaperType.LIVE, "Luxury", "luxury_gold"),
        WallpaperItem("live_luxury_diamonds", "Diamond Shine", WallpaperType.LIVE, "Luxury", "diamond_shine"),
        WallpaperItem("live_nature_ocean", "Ocean Motion", WallpaperType.LIVE, "Nature", "ocean_motion"),
        WallpaperItem("live_nature_rain", "Rainy Forest", WallpaperType.LIVE, "Nature", "rain_forest"),
        WallpaperItem("live_love_hearts", "Rose Hearts", WallpaperType.LIVE, "Love", "rose_hearts"),
        WallpaperItem("live_love_names", "Couple Glow", WallpaperType.LIVE, "Love", "couple_glow"),
        WallpaperItem("live_neon_waves", "Neon Blue Waves", WallpaperType.LIVE, "3D / Abstract", "neon_waves"),
        WallpaperItem("live_galaxy_depth", "Galaxy Depth", WallpaperType.LIVE, "3D / Abstract", "galaxy_depth"),

        WallpaperItem("static_islamic_mosque", "Golden Mosque", WallpaperType.STATIC, "Islamic", "mosque_static"),
        WallpaperItem("static_islamic_crescent", "Crescent Night", WallpaperType.STATIC, "Islamic", "crescent_static"),
        WallpaperItem("static_sports_pitch", "Football Pitch", WallpaperType.STATIC, "Sports", "pitch_static"),
        WallpaperItem("static_sports_medal", "Champion Medal", WallpaperType.STATIC, "Sports", "medal_static"),
        WallpaperItem("static_luxury_black_gold", "Black Gold 4K", WallpaperType.STATIC, "Luxury", "black_gold_static"),
        WallpaperItem("static_luxury_marble", "Premium Marble", WallpaperType.STATIC, "Luxury", "marble_static"),
        WallpaperItem("static_nature_mountain", "Mountain Calm", WallpaperType.STATIC, "Nature", "mountain_static"),
        WallpaperItem("static_nature_sunset", "Sunset Lake", WallpaperType.STATIC, "Nature", "sunset_static"),
        WallpaperItem("static_love_gradient", "Love Gradient", WallpaperType.STATIC, "Love", "love_static"),
        WallpaperItem("static_love_minimal", "Minimal Heart", WallpaperType.STATIC, "Love", "minimal_heart_static"),
        WallpaperItem("static_abstract_geometry", "3D Geometry", WallpaperType.STATIC, "3D / Abstract", "geometry_static"),
        WallpaperItem("static_abstract_matrix", "Digital Matrix", WallpaperType.STATIC, "3D / Abstract", "matrix_static")
    )

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
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(5, 8, 22))
            setPadding(dp(16), dp(18), dp(16), dp(10))
        }

        root.addView(TextView(this).apply {
            text = "3D Live Wallpapers"
            setTextColor(Color.WHITE)
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Islamic • Sports • Luxury • Nature • Love • 3D"
            setTextColor(Color.rgb(170, 180, 205))
            textSize = 14f
            setPadding(0, dp(4), 0, dp(14))
        })

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tabs.addView(tabButton("Live") { selectedType = WallpaperType.LIVE; selectedCategory = "All"; renderHome() })
        tabs.addView(tabButton("Static") { selectedType = WallpaperType.STATIC; selectedCategory = "All"; renderHome() })
        tabs.addView(tabButton("Favorites") { renderFavorites() })
        root.addView(tabs)

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    private fun tabButton(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(22, 28, 55), dp(18))
        setPadding(dp(12), dp(11), dp(12), dp(11))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(dp(4), 0, dp(4), dp(16))
        }
    }

    private fun renderHome() {
        val items = wallpapers.filter { it.type == selectedType && (selectedCategory == "All" || it.category == selectedCategory) }
        renderList(if (selectedType == WallpaperType.LIVE) "Live Wallpapers" else "Static Wallpapers", items, showCategories = true)
    }

    private fun renderFavorites() {
        val items = wallpapers.filter { favoriteIds.contains(it.id) }
        renderList("Favorites", items, showCategories = false)
    }

    private fun renderList(title: String, items: List<WallpaperItem>, showCategories: Boolean) {
        content.removeAllViews()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(24)) }
        box.addView(TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(12))
        })
        if (showCategories) box.addView(categoryBar())

        if (items.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "No wallpapers here yet."
                setTextColor(Color.rgb(170, 180, 205))
                textSize = 15f
                setPadding(0, dp(20), 0, 0)
            })
        } else {
            items.chunked(2).forEach { rowItems ->
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                rowItems.forEach { row.addView(card(it)) }
                if (rowItems.size == 1) row.addView(View(this), LinearLayout.LayoutParams(0, dp(215), 1f))
                box.addView(row)
            }
        }
        scroll.addView(box)
        content.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun categoryBar(): HorizontalScrollView {
        val scroller = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        categories.forEach { cat -> row.addView(categoryChip(cat)) }
        scroller.addView(row)
        return scroller
    }

    private fun categoryChip(cat: String): TextView = TextView(this).apply {
        text = cat
        val selected = cat == selectedCategory
        setTextColor(if (selected) Color.rgb(5, 8, 22) else Color.WHITE)
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(if (selected) Color.WHITE else Color.rgb(22, 28, 55), dp(18))
        setPadding(dp(13), dp(9), dp(13), dp(9))
        setOnClickListener { selectedCategory = cat; renderHome() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, dp(8), dp(16))
        }
    }

    private fun card(item: WallpaperItem): FrameLayout {
        val frame = FrameLayout(this).apply {
            background = rounded(Color.rgb(10, 14, 30), dp(22))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { openPreview(item) }
        }
        frame.addView(ArtView(this, item, animate = item.type == WallpaperType.LIVE), FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        frame.addView(TextView(this).apply {
            text = if (item.type == WallpaperType.LIVE) "LIVE" else "STATIC"
            setTextColor(Color.WHITE); textSize = 10f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            background = rounded(Color.argb(150, 0, 0, 0), dp(12)); setPadding(dp(8), dp(4), dp(8), dp(4))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START))
        frame.addView(TextView(this).apply {
            text = if (favoriteIds.contains(item.id)) "★" else "☆"
            setTextColor(Color.WHITE); textSize = 26f; gravity = Gravity.CENTER
            setOnClickListener { toggleFavorite(item); renderHome() }
        }, FrameLayout.LayoutParams(dp(44), dp(44), Gravity.TOP or Gravity.END))
        frame.addView(TextView(this).apply {
            text = item.title
            setTextColor(Color.WHITE); textSize = 15f; typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(10f, 0f, 3f, Color.BLACK)
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START))
        frame.layoutParams = LinearLayout.LayoutParams(0, dp(215), 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(10)) }
        return frame
    }

    private fun openPreview(item: WallpaperItem) {
        content.removeAllViews()
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(24)) }
        box.addView(TextView(this).apply { text = item.title; setTextColor(Color.WHITE); textSize = 24f; typeface = Typeface.DEFAULT_BOLD })
        box.addView(TextView(this).apply {
            text = "${item.category} • ${if (item.type == WallpaperType.LIVE) "Animated Live" else "Static 4K"}"
            setTextColor(Color.rgb(170, 180, 205)); textSize = 14f; setPadding(0, dp(4), 0, dp(12))
        })
        box.addView(ArtView(this, item, animate = item.type == WallpaperType.LIVE), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(470)).apply { setMargins(0, 0, 0, dp(14)) })
        box.addView(action(if (favoriteIds.contains(item.id)) "Remove from Favorites" else "Add to Favorites") { toggleFavorite(item); openPreview(item) })
        if (item.type == WallpaperType.LIVE) {
            box.addView(action("Set as Live Wallpaper") { saveLive(item); openLiveWallpaperPicker() })
        } else {
            box.addView(action("Set as Static Wallpaper") { setStatic(item) })
        }
        box.addView(action("Back") { renderHome() })
        scroll.addView(box)
        content.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun action(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label; setTextColor(Color.WHITE); textSize = 16f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
        background = rounded(Color.rgb(35, 43, 80), dp(18)); setPadding(dp(14), dp(14), dp(14), dp(14)); setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, dp(10)) }
    }

    private fun toggleFavorite(item: WallpaperItem) {
        if (favoriteIds.contains(item.id)) favoriteIds.remove(item.id) else favoriteIds.add(item.id)
        prefs.edit().putStringSet("favorites", favoriteIds).apply()
    }

    private fun saveLive(item: WallpaperItem) {
        prefs.edit().putString("live_design", item.design).putString("live_title", item.title).apply()
    }

    private fun openLiveWallpaperPicker() {
        val component = ComponentName(this, MotionLiveWallpaperService::class.java)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply { putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component) }
        try { startActivity(intent) } catch (_: Exception) { startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)) }
    }

    private fun setStatic(item: WallpaperItem) {
        try {
            val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            DemoArt.draw(canvas, 1080f, 1920f, item.design, 0f, item.title)
            WallpaperManager.getInstance(this).setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
            Toast.makeText(this, "Static wallpaper applied", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not set wallpaper", Toast.LENGTH_LONG).show()
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private class ArtView(context: Context, private val item: WallpaperItem, private val animate: Boolean) : View(context) {
        override fun onDraw(canvas: Canvas) {
            val t = if (animate) System.currentTimeMillis() / 1000f else 0f
            DemoArt.draw(canvas, width.toFloat(), height.toFloat(), item.design, t, item.title)
            if (animate) postInvalidateDelayed(33L)
        }
    }
}

object DemoArt {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(c: Canvas, w: Float, h: Float, design: String, t: Float, title: String) {
        when (design) {
            "islamic_orbit" -> islamic(c, w, h, t, animated = true)
            "islamic_lantern" -> lantern(c, w, h, t)
            "football_energy" -> football(c, w, h, t)
            "stadium_lights" -> stadium(c, w, h, t)
            "luxury_gold" -> luxury(c, w, h, t)
            "diamond_shine" -> diamond(c, w, h, t)
            "ocean_motion" -> ocean(c, w, h, t)
            "rain_forest" -> rain(c, w, h, t)
            "rose_hearts" -> hearts(c, w, h, t)
            "couple_glow" -> couple(c, w, h, t)
            "neon_waves" -> neon(c, w, h, t)
            "galaxy_depth" -> galaxy(c, w, h, t)
            "mosque_static" -> islamic(c, w, h, 0f, animated = false)
            "crescent_static" -> crescent(c, w, h)
            "pitch_static" -> pitch(c, w, h)
            "medal_static" -> medal(c, w, h)
            "black_gold_static" -> blackGold(c, w, h)
            "marble_static" -> marble(c, w, h)
            "mountain_static" -> mountain(c, w, h)
            "sunset_static" -> sunset(c, w, h)
            "love_static" -> loveStatic(c, w, h)
            "minimal_heart_static" -> minimalHeart(c, w, h)
            "geometry_static" -> geometry(c, w, h)
            "matrix_static" -> matrix(c, w, h)
            else -> neon(c, w, h, t)
        }
        label(c, w, h, title)
    }

    private fun bg(c: Canvas, w: Float, h: Float, colors: IntArray) { p.shader = LinearGradient(0f, 0f, w, h, colors, null, Shader.TileMode.CLAMP); c.drawRect(0f, 0f, w, h, p); p.shader = null; p.style = Paint.Style.FILL; p.alpha = 255 }
    private fun islamic(c: Canvas, w: Float, h: Float, t: Float, animated: Boolean) { bg(c,w,h,intArrayOf(Color.rgb(0,8,22),Color.rgb(0,85,80),Color.rgb(210,165,65))); p.style=Paint.Style.STROKE;p.strokeWidth=4f;p.color=Color.argb(180,255,225,130); for(i in 0..7)c.drawCircle(w*.5f,h*.42f,w*(.1f+i*.045f)+(if(animated)sin(t+i)*8f else 0f),p); p.style=Paint.Style.FILL;p.color=Color.argb(230,255,230,150);c.drawCircle(w*.47f,h*.35f,w*.11f,p);p.color=Color.rgb(0,35,45);c.drawCircle(w*.52f,h*.33f,w*.11f,p); p.color=Color.argb(210,255,235,170); c.drawRect(w*.18f,h*.62f,w*.82f,h*.80f,p) }
    private fun lantern(c: Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(4,12,28),Color.rgb(20,80,70),Color.rgb(255,175,60))); p.color=Color.argb(230,255,220,120); val x=w*.5f+sin(t)*20f; c.drawRoundRect(RectF(x-w*.13f,h*.25f,x+w*.13f,h*.62f),24f,24f,p); p.color=Color.argb(90,255,255,200); c.drawCircle(x,h*.45f,w*.28f,p) }
    private fun football(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(0,30,15),Color.rgb(0,120,55),Color.rgb(15,220,90))); p.color=Color.WHITE; val x=w*.5f+sin(t*1.5f)*w*.22f; val y=h*.43f+cos(t*1.8f)*h*.08f; c.drawCircle(x,y,w*.13f,p); p.color=Color.BLACK; for(i in 0..5)c.drawCircle(x+sin(i*1.2f+t)*w*.06f,y+cos(i*1.2f+t)*w*.06f,w*.025f,p); p.color=Color.argb(95,255,255,255); for(i in 0..5)c.drawLine(0f,h*(.2f+i*.12f),w,h*(.2f+i*.12f),p) }
    private fun stadium(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(4,8,20),Color.rgb(20,80,120),Color.rgb(0,210,255))); p.color=Color.argb(110,255,255,255); for(i in 0..5){ val x=w*(.12f+i*.16f); c.drawLine(x,h*.05f,w*.5f+sin(t+i)*60f,h*.55f,p)}; p.color=Color.rgb(0,120,60); c.drawOval(RectF(w*.08f,h*.55f,w*.92f,h*.86f),p) }
    private fun luxury(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.BLACK,Color.rgb(70,42,6),Color.rgb(255,185,45))); p.style=Paint.Style.STROKE;p.strokeWidth=5f;p.color=Color.rgb(255,220,95); c.drawRoundRect(RectF(w*.13f,h*.15f,w*.87f,h*.78f),42f+sin(t)*10f,42f+sin(t)*10f,p); p.style=Paint.Style.FILL }
    private fun diamond(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(8,8,15),Color.rgb(30,30,60),Color.rgb(210,210,255))); p.color=Color.argb(190,255,255,255); for(i in 0..9){ val x=w*(.15f+i*.08f); val y=h*(.25f+.08f*sin(t+i)); val path=Path(); path.moveTo(x,y-28);path.lineTo(x+28,y);path.lineTo(x,y+28);path.lineTo(x-28,y);path.close();c.drawPath(path,p)} }
    private fun ocean(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(0,35,70),Color.rgb(0,165,205),Color.rgb(160,245,255))); for(layer in 0..5){ val path=Path(); val base=h*(.38f+layer*.09f); path.moveTo(0f,h); path.lineTo(0f,base); var x=0f; while(x<=w){path.lineTo(x,base+sin(x/w*7f+t+layer)*(20f+layer*5f));x+=14f}; path.lineTo(w,h); path.close(); p.color=Color.argb(70+layer*22,255,255,255); c.drawPath(path,p)} }
    private fun rain(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(0,20,18),Color.rgb(0,80,60),Color.rgb(40,160,120))); p.color=Color.argb(150,210,255,240); for(i in 0..45){ val x=w*((i*37)%100)/100f; val y=(h*((i*19)%100)/100f+t*(90+i%6*15))%h; c.drawLine(x,y,x-10,y+35,p)} }
    private fun hearts(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(42,5,30),Color.rgb(210,35,105),Color.rgb(255,100,165))); p.color=Color.argb(130,255,255,255); for(i in 0..14)heart(c,w*(.14f+i*.06f),h*.82f-((t*70+i*85)%(h*.68f)),12f+i%7) }
    private fun couple(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(35,5,40),Color.rgb(130,40,180),Color.rgb(255,95,170))); p.color=Color.argb(90,255,255,255); c.drawCircle(w*.38f+sin(t)*8,h*.42f,w*.22f,p); c.drawCircle(w*.62f-sin(t)*8,h*.42f,w*.22f,p) }
    private fun neon(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(2,8,35),Color.rgb(0,220,255),Color.rgb(120,80,255))); p.style=Paint.Style.STROKE; for(layer in 0..5){ val path=Path(); val base=h*(.28f+layer*.11f); path.moveTo(0f,base); var x=0f; while(x<=w){path.lineTo(x,base+sin(x/w*8f+t*(1.1f+layer*.18f))*(22f+layer*7f));x+=14f}; p.strokeWidth=4f+layer;p.color=Color.argb(150-layer*15,255,255,255);c.drawPath(path,p)}; p.style=Paint.Style.FILL }
    private fun galaxy(c:Canvas,w:Float,h:Float,t:Float){ bg(c,w,h,intArrayOf(Color.rgb(3,2,22),Color.rgb(65,20,100),Color.rgb(0,170,230))); p.color=Color.argb(65,190,90,255); c.drawCircle(w*(.58f+.04f*sin(t*.4f)),h*.42f,w*.48f,p); p.color=Color.WHITE; for(i in 0..70){p.alpha=70+i%160;c.drawCircle(w*((sin(i*12.2f+t*.3f)+1)/2f),h*((cos(i*8.7f+t*.25f)+1)/2f),1.5f+i%4,p)};p.alpha=255 }
    private fun crescent(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(0,5,25),Color.rgb(10,40,80),Color.rgb(210,190,120))); p.color=Color.rgb(255,230,150); c.drawCircle(w*.42f,h*.35f,w*.16f,p); p.color=Color.rgb(0,5,25); c.drawCircle(w*.49f,h*.31f,w*.16f,p) }
    private fun pitch(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(0,70,25),Color.rgb(0,150,55),Color.rgb(0,90,35))); p.style=Paint.Style.STROKE;p.strokeWidth=5f;p.color=Color.WHITE;c.drawRect(w*.12f,h*.16f,w*.88f,h*.84f,p);c.drawLine(w*.5f,h*.16f,w*.5f,h*.84f,p);c.drawCircle(w*.5f,h*.5f,w*.12f,p);p.style=Paint.Style.FILL }
    private fun medal(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(15,15,35),Color.rgb(35,60,120),Color.rgb(255,190,60))); p.color=Color.rgb(255,210,80); c.drawCircle(w*.5f,h*.45f,w*.22f,p); p.color=Color.rgb(25,25,55); c.drawCircle(w*.5f,h*.45f,w*.13f,p) }
    private fun blackGold(c:Canvas,w:Float,h:Float){ luxury(c,w,h,0f) }
    private fun marble(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(25,25,32),Color.rgb(210,210,220),Color.rgb(80,80,95))); p.color=Color.argb(70,255,255,255); for(i in 0..9)c.drawLine(w*(i*.12f),0f,w*(i*.12f+.4f),h,p) }
    private fun mountain(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(14,30,50),Color.rgb(85,150,180),Color.rgb(235,245,255))); p.color=Color.rgb(240,245,255); val path=Path(); path.moveTo(0f,h*.75f); path.lineTo(w*.3f,h*.42f); path.lineTo(w*.55f,h*.75f); path.lineTo(w*.72f,h*.5f); path.lineTo(w,h*.78f); path.lineTo(w,h); path.lineTo(0f,h); path.close(); c.drawPath(path,p) }
    private fun sunset(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(40,10,45),Color.rgb(255,95,85),Color.rgb(255,210,90))); p.color=Color.argb(220,255,230,140); c.drawCircle(w*.5f,h*.38f,w*.18f,p) }
    private fun loveStatic(c:Canvas,w:Float,h:Float){ hearts(c,w,h,0f) }
    private fun minimalHeart(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(45,5,30),Color.rgb(120,20,80),Color.rgb(250,90,145))); p.color=Color.WHITE; heart(c,w*.5f,h*.45f,w*.12f) }
    private fun geometry(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(10,16,35),Color.rgb(60,70,180),Color.rgb(0,230,210))); p.style=Paint.Style.STROKE;p.strokeWidth=5f;p.color=Color.argb(150,255,255,255); for(i in 0..7)c.drawRoundRect(RectF(w*(.08f+i*.04f),h*(.12f+i*.05f),w*(.88f-i*.03f),h*(.88f-i*.04f)),30f,30f,p); p.style=Paint.Style.FILL }
    private fun matrix(c:Canvas,w:Float,h:Float){ bg(c,w,h,intArrayOf(Color.rgb(0,5,0),Color.rgb(0,60,30),Color.rgb(0,180,90))); p.color=Color.argb(150,170,255,190); p.typeface=Typeface.MONOSPACE;p.textSize=min(w,h)*.05f; for(col in 0..12)for(row in 0..10)c.drawText(if((col+row)%2==0)"01" else "10",col*w/10f,row*h/10f,p) }
    private fun heart(c:Canvas,x:Float,y:Float,s:Float){ val path=Path(); path.moveTo(x,y+s); path.cubicTo(x-2*s,y-.5f*s,x-s,y-2*s,x,y-s); path.cubicTo(x+s,y-2*s,x+2*s,y-.5f*s,x,y+s); c.drawPath(path,p) }
    private fun label(c:Canvas,w:Float,h:Float,title:String){ p.shader=null;p.style=Paint.Style.FILL;p.color=Color.WHITE;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=min(w,h)*.055f;p.setShadowLayer(14f,0f,4f,Color.BLACK);c.drawText(title.take(24),w*.07f,h*.84f,p);p.clearShadowLayer();p.typeface=Typeface.DEFAULT }
}
