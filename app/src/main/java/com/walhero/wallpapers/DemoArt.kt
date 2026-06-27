package com.walhero.wallpapers

import android.graphics.Bitmap
import android.graphics.Canvas

object DemoArt {
    private var currentKey = ""
    private var bitmap: Bitmap? = null

    private data class LiveMedia(val url: String, val motion: String)

    private fun photo(tags: String, lock: Int): String = "https://loremflickr.com/1080/1920/$tags?lock=$lock"

    private fun mediaForTitle(title: String): LiveMedia = when (title) {
        "Makkah Night Motion" -> LiveMedia(photo("kaaba,islamic", 1101), "glow")
        "Golden Mosque Lights" -> LiveMedia(photo("mosque,architecture", 1102), "glow")
        "Ramadan Lantern" -> LiveMedia(photo("ramadan,lantern", 1103), "particles")
        "Crescent Sky" -> LiveMedia(photo("crescent,night", 1104), "stars")
        "Prayer Calm" -> LiveMedia(photo("prayer,mosque", 1105), "slow_zoom")
        "Football Stadium" -> LiveMedia(photo("football,stadium", 1201), "stadium")
        "Soccer Energy" -> LiveMedia(photo("soccer,ball", 1202), "energy")
        "Champion Arena" -> LiveMedia(photo("sports,arena", 1203), "stadium")
        "Running Power" -> LiveMedia(photo("running,athlete", 1204), "energy")
        "Basketball Lights" -> LiveMedia(photo("basketball,court", 1205), "stadium")
        "Black Gold Motion" -> LiveMedia(photo("luxury,gold", 1301), "gold")
        "Premium Car Shine" -> LiveMedia(photo("luxury,car", 1302), "shine")
        "Diamond Glow" -> LiveMedia(photo("diamond,jewelry", 1303), "shine")
        "Luxury Hotel Night" -> LiveMedia(photo("luxury,hotel", 1304), "slow_zoom")
        "Elegant Watch" -> LiveMedia(photo("luxury,watch", 1305), "gold")
        "Ocean Waves" -> LiveMedia(photo("ocean,waves", 1401), "wave")
        "Rain Forest" -> LiveMedia(photo("rain,forest", 1402), "rain")
        "Mountain Clouds" -> LiveMedia(photo("mountain,clouds", 1403), "slow_zoom")
        "Waterfall Fresh" -> LiveMedia(photo("waterfall,nature", 1404), "rain")
        "Northern Sky" -> LiveMedia(photo("aurora,sky", 1405), "stars")
        "Romantic Roses" -> LiveMedia(photo("roses,love", 1501), "hearts")
        "Couple Sunset" -> LiveMedia(photo("couple,sunset", 1502), "hearts")
        "Red Heart Glow" -> LiveMedia(photo("heart,romantic", 1503), "hearts")
        "Love Lights" -> LiveMedia(photo("love,lights", 1504), "glow")
        "Wedding Dream" -> LiveMedia(photo("wedding,romantic", 1505), "slow_zoom")
        "Neon City" -> LiveMedia(photo("neon,city", 1601), "neon")
        "Galaxy Depth" -> LiveMedia(photo("galaxy,space", 1602), "stars")
        "Cyber Light" -> LiveMedia(photo("cyberpunk,neon", 1603), "neon")
        "Digital Abstract" -> LiveMedia(photo("abstract,technology", 1604), "particles")
        "3D Future" -> LiveMedia(photo("futuristic,3d", 1605), "shine")
        else -> LiveMedia(photo("neon,city", 1601), "neon")
    }

    fun draw(canvas: Canvas, width: Float, height: Float, design: String, time: Float, title: String) {
        val media = mediaForTitle(title)
        if (media.url != currentKey) {
            currentKey = media.url
            bitmap = null
            RemoteBitmapStore.load(media.url) { loaded -> bitmap = loaded }
        }
        RealWallpaperRenderer.draw(canvas, bitmap, media.motion, true, true)
    }
}
