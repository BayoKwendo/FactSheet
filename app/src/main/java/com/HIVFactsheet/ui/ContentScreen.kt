package com.HIVFactsheet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.HIVFactsheet.data.Block
import com.HIVFactsheet.data.Repository
import com.HIVFactsheet.data.Screen
import com.HIVFactsheet.data.Span

private val Highlight = Color(0xFFFFEB3B)

/**
 * Renders any registry [screen] that is not an external web page: its native
 * content blocks (if any) followed by cards to its child screens. Includes the
 * same in-page keyword search the original WebView offered.
 */
@Composable
fun ContentScreen(
    screen: Screen,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val blocks = remember(screen.id) {
        screen.content?.let { Repository.content(it) } ?: emptyList()
    }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val hasContent = blocks.isNotEmpty()

    AppScaffold(
        title = screen.title,
        onBack = onBack,
        onSearchToggle = if (hasContent) {
            { searchOpen = !searchOpen; if (!searchOpen) query = "" }
        } else null,
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (searchOpen && hasContent) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text("Type keyword…") },
                    trailingIcon = {
                        IconButton(onClick = { query = ""; searchOpen = false }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Close search")
                        }
                    },
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }

            // "Latest guidance" blocks are prepended; surface them as a callout.
            val callout = blocks.takeWhile { it.updated }
            val body = blocks.drop(callout.size)

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (callout.isNotEmpty()) item { GuidanceCallout(callout, query) }
                items(body) { block -> BlockView(block, query) }

                if (screen.children.isNotEmpty()) {
                    if (hasContent) item { Spacer(Modifier.height(8.dp)) }
                    itemsIndexed(screen.children) { _, childId ->
                        val child = Repository.screen(childId)
                        if (child != null) {
                            TopicCard(title = child.title, onClick = { onOpen(childId) })
                        }
                    }
                }

                if (!hasContent && screen.children.isEmpty()) {
                    item { Text("Content coming soon.") }
                }
            }
        }
    }
}

/** Highlighted card that frames the refreshed "latest guidance" blocks. */
@Composable
private fun GuidanceCallout(blocks: List<Block>, query: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F5E6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Check, contentDescription = null,
                    tint = Color(0xFF1D7A10), modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "UPDATED INFORMATION",
                    color = Color(0xFF1D7A10),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp,
                )
            }
            blocks.forEach { b ->
                val isGenericHead = b is Block.Heading &&
                    b.spans.joinToString("") { it.text }.trim().equals("Latest guidance", true)
                if (!isGenericHead) BlockView(b, query)
            }
        }
    }
}

@Composable
private fun BlockView(block: Block, query: String) {
    when (block) {
        is Block.Heading -> Text(
            text = highlighted(block.spans, query),
            fontSize = when (block.level) { 1 -> 24.sp; 2 -> 20.sp; else -> 18.sp },
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
        )
        is Block.Para -> Text(
            text = highlighted(block.spans, query),
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        is Block.Bullet -> Row(Modifier.fillMaxWidth().padding(start = 4.dp)) {
            Text(if (block.ordered) "•  " else "•  ", fontSize = 16.sp)
            Text(highlighted(block.spans, query), fontSize = 16.sp, lineHeight = 24.sp)
        }
        is Block.Img -> {
            val context = LocalContext.current
            val bmp = remember(block.src) { Repository.image(context, block.src) }
            if (bmp != null) {
                Spacer(Modifier.height(2.dp))
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Build styled text from spans, overlaying a highlight on query matches. */
private fun highlighted(spans: List<Span>, query: String): AnnotatedString {
    val base = buildAnnotatedString {
        for (s in spans) {
            withStyle(
                SpanStyle(
                    fontWeight = if (s.bold) FontWeight.Bold else null,
                    fontStyle = if (s.italic) FontStyle.Italic else null,
                    color = s.color?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                        ?: Color.Unspecified,
                )
            ) { append(s.text) }
        }
    }
    if (query.isBlank()) return base
    val text = base.text
    val builder = AnnotatedString.Builder(base)
    var index = text.indexOf(query, ignoreCase = true)
    while (index >= 0) {
        builder.addStyle(SpanStyle(background = Highlight), index, index + query.length)
        index = text.indexOf(query, index + query.length, ignoreCase = true)
    }
    return builder.toAnnotatedString()
}
