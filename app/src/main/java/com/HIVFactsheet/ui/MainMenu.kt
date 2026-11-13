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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val pagerState = rememberPagerState(pageCount = { reg.tabs.size })

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "FactSheet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = BrandPurple,
                    modifier = Modifier.padding(16.dp),
                )
                HorizontalDivider()
                fun close() = scope.launch { drawerState.close() }
                DrawerLink("Home", Icons.AutoMirrored.Filled.List) { close() }
                DrawerLink("Bookmarks", Icons.Filled.Bookmark) { close(); onBookmarks() }
                DrawerLink("HIV Research", Icons.AutoMirrored.Filled.List) { close(); onOpen("Main43Activity") }
                DrawerLink("AMPATH", Icons.AutoMirrored.Filled.List) { close(); onOpen("ampath") }
                DrawerLink("NASCOP VL System", Icons.AutoMirrored.Filled.List) { close(); onOpen("videos") }
                HorizontalDivider()
                DrawerLink(
                    if (isDark) "Light mode" else "Dark mode",
                    if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                ) { Prefs.setDark(!isDark) }
                DrawerLink("Share", Icons.Filled.Share) { close(); share() }
                DrawerLink("About Us", Icons.Filled.Info) { close(); onOpen("AboutActivity") }
            }
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("FactSheet", fontWeight = FontWeight.Bold, color = BrandCyan) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { share() }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
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
                    val hub = Repository.screen(reg.tabs[page].id)
                    if (hub != null) HubList(hub, onOpen)
                }
            }
        }
    }
}

@Composable
fun HubList(hub: Screen, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        // Smart finder on the health-center tab: open maps to nearby CCCs.
        if (hub.id == "you6") {
            item {
                FindNearbyCard {
                    val q = Uri.encode("Comprehensive Care Centre HIV clinic near me")
                    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$q")
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                }
            }
        }
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
            Text("Search all topics…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Big action card that launches maps to the nearest HIV care centres. */
@Composable
private fun FindNearbyCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCyan, contentColor = BrandPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandPurple)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Find a CCC near you", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandPurple)
                Text(
                    "Opens maps to HIV care centres near your location",
                    fontSize = 13.sp,
                    color = BrandPurple.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun DrawerLink(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        icon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}
