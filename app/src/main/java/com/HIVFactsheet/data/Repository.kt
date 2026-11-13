package com.HIVFactsheet.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads the two generated data files (gen/screens.json, gen/content.json) from
 * assets and exposes the navigation registry and per-screen native content.
 *
 * org.json (bundled with Android) is used instead of a JSON library to keep the
 * APK small. Content blocks are parsed lazily per screen and cached.
 */
object Repository {

    lateinit var registry: Registry
        private set

    private lateinit var contentRoot: JSONObject
    private val contentCache = HashMap<String, List<Block>>()
    private val imageCache = HashMap<String, ImageBitmap?>()
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val assets = context.assets
        registry = parseRegistry(
            JSONObject(assets.open("gen/screens.json").readBytesUtf8())
        )
        contentRoot = JSONObject(assets.open("gen/content.json").readBytesUtf8())
        initialized = true
    }

    fun screen(id: String): Screen? = registry.screens[id]

    fun content(key: String): List<Block> = contentCache.getOrPut(key) {
        if (!contentRoot.has(key)) emptyList()
        else parseBlocks(contentRoot.getJSONArray(key))
    }

    /** Decode an asset image once, downsampled to a sane bound, and cache it. */
    fun image(context: Context, path: String): ImageBitmap? = imageCache.getOrPut(path) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, 1080)
            }
            context.assets.open(path).use {
                BitmapFactory.decodeStream(it, null, opts)?.asImageBitmap()
            }
        }.getOrNull()
    }

    private fun sampleSize(width: Int, max: Int): Int {
        var s = 1
        while (width / s > max) s *= 2
        return s
    }

    // --- parsing -----------------------------------------------------------

    private fun parseRegistry(o: JSONObject): Registry {
        val tabsArr = o.getJSONArray("tabs")
        val tabs = (0 until tabsArr.length()).map { i ->
            val t = tabsArr.getJSONObject(i)
            Tab(t.getString("id"), t.getString("title"))
        }
        val screensObj = o.getJSONObject("screens")
        val screens = HashMap<String, Screen>(screensObj.length())
        for (id in screensObj.keys()) {
            val s = screensObj.getJSONObject(id)
            val childArr = s.optJSONArray("children")
            val children = if (childArr == null) emptyList()
            else (0 until childArr.length()).map { childArr.getString(it) }
            screens[id] = Screen(
                id = s.getString("id"),
                title = s.optString("title", ""),
                content = s.optStringOrNull("content"),
                children = children,
                url = s.optStringOrNull("url"),
            )
        }
        return Registry(
            start = o.getString("start"),
            tabs = tabs,
            screens = screens,
            vlUrl = o.getString("vlUrl"),
            youtubeUrl = o.getString("youtubeUrl"),
        )
    }

    private fun parseBlocks(arr: JSONArray): List<Block> = buildList {
        for (i in 0 until arr.length()) {
            val b = arr.getJSONObject(i)
            when (b.getString("t")) {
                "p" -> add(Block.Para(parseSpans(b.getJSONArray("s"))))
                "h" -> add(Block.Heading(b.optInt("lvl", 2), parseSpans(b.getJSONArray("s"))))
                "li" -> add(Block.Bullet(parseSpans(b.getJSONArray("s")), b.optInt("o", 0) == 1))
                "img" -> add(Block.Img(b.getString("src")))
            }
        }
    }

    private fun parseSpans(arr: JSONArray): List<Span> = buildList {
        for (i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            add(
                Span(
                    text = s.getString("text"),
                    bold = s.optInt("b", 0) == 1,
                    italic = s.optInt("i", 0) == 1,
                    color = s.optStringOrNull("c"),
                )
            )
        }
    }
}

// --- small helpers on org.json --------------------------------------------

private fun java.io.InputStream.readBytesUtf8(): String =
    use { String(it.readBytes(), Charsets.UTF_8) }

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
