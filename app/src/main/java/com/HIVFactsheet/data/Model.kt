package com.HIVFactsheet.data

/** A styled run of text inside a content block. */
data class Span(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val color: String? = null,
)

/**
 * A single native content block converted from the original HTML.
 * [updated] marks blocks added during the "latest guidance" refresh so the UI
 * can present them as a distinct callout.
 */
sealed interface Block {
    val updated: Boolean

    data class Para(val spans: List<Span>, override val updated: Boolean = false) : Block
    data class Heading(val level: Int, val spans: List<Span>, override val updated: Boolean = false) : Block
    data class Bullet(val spans: List<Span>, val ordered: Boolean, override val updated: Boolean = false) : Block
    data class Img(val src: String, override val updated: Boolean = false) : Block
}

/**
 * One destination in the app. A screen may carry native [content], a list of
 * [children] to navigate to, an external [url], or any combination.
 */
data class Screen(
    val id: String,
    val title: String,
    val content: String? = null,
    val children: List<String> = emptyList(),
    val url: String? = null,
)

data class Tab(val id: String, val title: String)

data class Registry(
    val start: String,
    val tabs: List<Tab>,
    val screens: Map<String, Screen>,
    val vlUrl: String,
    val youtubeUrl: String,
)
