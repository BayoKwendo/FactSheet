package com.HIVFactsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.HIVFactsheet.data.Repository
import com.HIVFactsheet.ui.ContentScreen
import com.HIVFactsheet.ui.FactSheetTheme
import com.HIVFactsheet.ui.MainMenu
import com.HIVFactsheet.ui.SplashScreen
import com.HIVFactsheet.ui.WebScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.init(applicationContext)
        setContent {
            FactSheetTheme { AppNav() }
        }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onDone = {
                nav.navigate("menu") { popUpTo("splash") { inclusive = true } }
            })
        }
        composable("menu") {
            MainMenu(onOpen = { id -> nav.navigate("screen/$id") })
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
