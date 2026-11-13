package com.HIVFactsheet

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.HIVFactsheet.data.Prefs
import com.HIVFactsheet.data.Repository
import com.HIVFactsheet.data.Speaker
import com.HIVFactsheet.ui.BookmarksScreen
import com.HIVFactsheet.ui.ContentScreen
import com.HIVFactsheet.ui.FactSheetTheme
import com.HIVFactsheet.ui.MainMenu
import com.HIVFactsheet.ui.SearchScreen
import com.HIVFactsheet.ui.WebScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        Repository.init(applicationContext)
        Prefs.init(applicationContext)
        Speaker.init(applicationContext)
        // Hold the single system splash briefly for a brand moment.
        val startMs = SystemClock.elapsedRealtime()
        splash.setKeepOnScreenCondition { SystemClock.elapsedRealtime() - startMs < 700 }
        setContent {
            FactSheetTheme { AppNav() }
        }
    }

    override fun onDestroy() {
        Speaker.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "menu") {
        composable("menu") {
            MainMenu(
                onOpen = { id -> nav.navigate("screen/$id") },
                onSearch = { nav.navigate("search") },
                onBookmarks = { nav.navigate("bookmarks") },
            )
        }
        composable("search") {
            SearchScreen(
                onOpen = { id -> nav.navigate("screen/$id") },
                onBack = { nav.popBackStack() },
            )
        }
        composable("bookmarks") {
            BookmarksScreen(
                onOpen = { id -> nav.navigate("screen/$id") },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = "screen/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val screen = Repository.screen(id)
            when {
                screen == null -> nav.popBackStack()
                screen.url != null -> WebScreen(
                    title = screen.title.ifBlank { "FactSheet" },
                    url = screen.url,
                    onBack = { nav.popBackStack() },
                )
                else -> ContentScreen(
                    screen = screen,
                    onOpen = { childId -> nav.navigate("screen/$childId") },
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}
