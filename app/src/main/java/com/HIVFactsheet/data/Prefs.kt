package com.HIVFactsheet.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tiny preferences store backed by SharedPreferences and exposed as Compose
 * state so the UI recomposes when bookmarks or the theme change.
 */
object Prefs {
    private const val FILE = "factsheet_prefs"
    private const val K_BOOKMARKS = "bookmarks"
    private const val K_DARK = "dark_mode"   // 0 = system, 1 = light, 2 = dark
    private const val K_FONT = "font_scale"

    const val FONT_MIN = 0.85f
    const val FONT_MAX = 1.6f
    private const val FONT_STEP = 0.15f

    private var sp: android.content.SharedPreferences? = null

    /** Observable list of bookmarked screen ids. */
    val bookmarks = mutableStateListOf<String>()

    /** null = follow system, true = dark, false = light. */
    var darkMode by mutableStateOf<Boolean?>(null)
        private set

    /** Reading text-size multiplier applied to content. */
    var fontScale by mutableStateOf(1.0f)
        private set

    fun init(context: Context) {
        if (sp != null) return
        val p = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        sp = p
        bookmarks.clear()
        bookmarks.addAll(p.getStringSet(K_BOOKMARKS, emptySet())!!.sorted())
        darkMode = when (p.getInt(K_DARK, 0)) { 1 -> false; 2 -> true; else -> null }
        fontScale = p.getFloat(K_FONT, 1.0f).coerceIn(FONT_MIN, FONT_MAX)
    }

    fun stepFont(up: Boolean) {
        val next = (fontScale + if (up) FONT_STEP else -FONT_STEP).coerceIn(FONT_MIN, FONT_MAX)
        fontScale = next
        sp?.edit()?.putFloat(K_FONT, next)?.apply()
    }

    fun isBookmarked(id: String) = id in bookmarks

    fun toggleBookmark(id: String) {
        if (id in bookmarks) bookmarks.remove(id) else bookmarks.add(id)
        sp?.edit()?.putStringSet(K_BOOKMARKS, bookmarks.toSet())?.apply()
    }

    fun setDark(value: Boolean?) {
        darkMode = value
        sp?.edit()?.putInt(K_DARK, when (value) { false -> 1; true -> 2; null -> 0 })?.apply()
    }
}
