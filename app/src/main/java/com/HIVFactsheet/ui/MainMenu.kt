package com.HIVFactsheet.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.HIVFactsheet.data.Prefs
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.HIVFactsheet.data.Repository
import com.HIVFactsheet.data.Screen
import kotlinx.coroutines.launch

/** Home screen: 4 swipeable topic tabs plus the navigation drawer. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(onOpen: (String) -> Unit, onSearch: () -> Unit, onBookmarks: () -> Unit) {
    val reg = Repository.registry
    val systemDark = isSystemInDarkTheme()
    val isDark = Prefs.darkMode ?: systemDark
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { reg.tabs.size })
    var menuOpen by remember { mutableStateOf(false) }

    fun share() {
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Get the FactSheet app — HIV facts, testing, treatment and youth health.",
            )
        }
        context.startActivity(Intent.createChooser(i, "Share via"))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("FactSheet", fontWeight = FontWeight.Bold, color = BrandCyan) },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Bookmarks") },
                            leadingIcon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
                            onClick = { menuOpen = false; onBookmarks() },
                        )
                        DropdownMenuItem(
                            text = { Text(if (isDark) "Light mode" else "Dark mode") },
                            leadingIcon = { Icon(if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode, contentDescription = null) },
                            onClick = { menuOpen = false; Prefs.setDark(!isDark) },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("HIV Research") }, onClick = { menuOpen = false; onOpen("Main43Activity") })
                        DropdownMenuItem(text = { Text("AMPATH") }, onClick = { menuOpen = false; onOpen("ampath") })
                        DropdownMenuItem(text = { Text("NASCOP VL System") }, onClick = { menuOpen = false; onOpen("videos") })
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                            onClick = { menuOpen = false; share() },
                        )
                        DropdownMenuItem(
                            text = { Text("About Us") },
                            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                            onClick = { menuOpen = false; onOpen("AboutActivity") },
                        )
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
    ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                // Prominent tap-to-search bar so information is easy to find.
                SearchEntryBar(onSearch)

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = BrandPurple,
                    edgePadding = 10.dp,
                    indicator = {},
                    divider = {},
                ) {
                    reg.tabs.forEachIndexed { i, tab ->
                        val selected = pagerState.currentPage == i
                        Tab(
                            selected = selected,
                            onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                            selectedContentColor = BrandPurple,
                            unselectedContentColor = BrandCyan,
                        ) {
                            Text(
                                tab.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp,
                                color = if (selected) BrandPurple else BrandCyan.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                                    .then(
                                        if (selected) Modifier
                                            .background(BrandCyan, RoundedCornerShape(50))
                                            .padding(horizontal = 16.dp, vertical = 7.dp)
                                        else Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                                    ),
                            )
                        }
                    }
                }
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val tabId = reg.tabs[page].id
                    if (tabId == "you6") {
                        HealthCenterScreen()
                    } else {
                        val hub = Repository.screen(tabId)
                        if (hub != null) HubList(hub, onOpen)
                    }
                }
            }
        }
    }

@Composable
fun HubList(hub: Screen, onOpen: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(hub.children) { childId ->
            val child = Repository.screen(childId) ?: return@items
            TopicCard(title = child.title, onClick = { onOpen(childId) })
        }
    }
}

/** Prominent tap-to-search bar shown at the top of the home screen. */
@Composable
private fun SearchEntryBar(onSearch: () -> Unit) {
    Surface(
        onClick = onSearch,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = BrandPurple)
            Spacer(Modifier.width(10.dp))
            Text("Search everything…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

