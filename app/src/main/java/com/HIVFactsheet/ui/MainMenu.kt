package com.HIVFactsheet.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
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
                DrawerLink("Videos", Icons.AutoMirrored.Filled.List) { close(); onOpen("Main39Activity") }
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
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = BrandPurple,
                    contentColor = BrandCyan,
                    edgePadding = 8.dp,
                ) {
                    reg.tabs.forEachIndexed { i, tab ->
                        Tab(
                            selected = pagerState.currentPage == i,
                            onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                            text = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        )
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
    val content = hub.content?.let { Repository.content(it) } ?: emptyList()
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
