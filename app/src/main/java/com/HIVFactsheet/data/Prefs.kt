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

    private var sp: android.content.SharedPreferences? = null

    /** Observable list of bookmarked screen ids. */
    val bookmarks = mutableStateListOf<String>()

    /** null = follow system, true = dark, false = light. */
    var darkMode by mutableStateOf<Boolean?>(null)
        private set

    fun init(context: Context) {
        if (sp != null) return
        val p = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        sp = p
        bookmarks.clear()
        bookmarks.addAll(p.getStringSet(K_BOOKMARKS, emptySet())!!.sorted())
        darkMode = when (p.getInt(K_DARK, 0)) { 1 -> false; 2 -> true; else -> null }
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
