package com.HIVFactsheet.ui

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard screen frame: purple toolbar with a marquee cyan title, an optional
 * back button, an optional bookmark toggle and an optional search toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    bookmarked: Boolean? = null,
    onBookmark: (() -> Unit)? = null,
    onSearchToggle: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.basicMarquee(),
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (onBookmark != null) {
                        IconButton(onClick = onBookmark) {
                            Icon(
                                if (bookmarked == true) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (bookmarked == true) "Remove bookmark" else "Bookmark",
                            )
                        }
                    }
                    if (onSearchToggle != null) {
                        IconButton(onClick = onSearchToggle) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandPurple,
                    titleContentColor = BrandCyan,
                    navigationIconContentColor = BrandCyan,
                    actionIconContentColor = BrandCyan,
                ),
            )
        },
        content = content,
    )
}

/** A tappable topic card used by the hub/menu/search/bookmark screens. */
@Composable
fun TopicCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandPurple, contentColor = BrandCyan),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Icon(topicIcon(title), contentDescription = null, tint = BrandCyan, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Text(
                text = title.ifBlank { "More" },
                color = BrandCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = BrandCyan)
        }
    }
}

/** Pick a representative icon for a topic from keywords in its title. */
fun topicIcon(title: String): ImageVector {
    val t = title.lowercase()
    fun has(vararg k: String) = k.any { it in t }
    return when {
        has("video", "dashboard") -> Icons.Filled.PlayCircle
        has("test", "counsel", "viral load", "cd4") -> Icons.Filled.Science
        has("prep", "pep", "prevent", "circumcis") -> Icons.Filled.Shield
        has("vaccin", "immun", "hepatitis", "flu", "pneumo") -> Icons.Filled.Vaccines
        has("treatment", "art ", "adhere", "drug interaction", "resist", "side effect") -> Icons.Filled.Medication
        has("pregnan", "pmtct", "conception", "infant", "breast", "child", "teenage preg") -> Icons.Filled.ChildCare
        has("tb", "overview", "stage", "transmission", "key population") -> Icons.Filled.Coronavirus
        has("mental", "emotion", "psych", "stress", "sleep") -> Icons.Filled.Psychology
        has("nutri", "diet", "food", "keeping fit", "healthy living") -> Icons.Filled.Restaurant
        has("condom", "safer sex", "sexual relation") -> Icons.Filled.Favorite
        has("menstr") -> Icons.Filled.WaterDrop
        has("assault", "gbv", "consent", "rape", "abortion", "violence") -> Icons.Filled.Gavel
        has("gender", "sexuality", "orientation", "relationship", "dating", "disclos") -> Icons.Filled.Diversity3
        has("support", "group", "peer", "network", "youth", "rafiki", "friend") -> Icons.Filled.Groups
        has("clinic", "hospital", "centre", "center", "hotline", "referral", "service", "rehab") -> Icons.Filled.LocalHospital
        has("call", "help") -> Icons.Filled.Call
        has("about", "ampath", "iyapp", "research", "policy", "quality") -> Icons.Filled.Info
        else -> Icons.AutoMirrored.Filled.Article
    }
}

/** Convenience for empty states. */
@Composable
fun CenteredNote(text: String, padding: PaddingValues) {
    Box(Modifier.fillMaxWidth().padding(padding).padding(24.dp)) {
        Text(text, color = MaterialTheme.colorScheme.onBackground)
    }
}
