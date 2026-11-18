package com.HIVFactsheet

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager

    // Immediate (mandatory) updates are enforced: if the user backs out without
    // completing the update, close the app so an outdated version can't be used.
    // The next launch re-prompts until they are on the latest published version.
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        Repository.init(applicationContext)
        Prefs.init(applicationContext)
        Speaker.init(applicationContext)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkForForcedUpdate()
        // Hold the single system splash briefly for a brand moment.
        val startMs = SystemClock.elapsedRealtime()
        splash.setKeepOnScreenCondition { SystemClock.elapsedRealtime() - startMs < 700 }
        setContent {
            FactSheetTheme { AppNav() }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume an update that Play reports as already in progress (e.g. the
        // user left mid-download), keeping the mandatory flow in front of them.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                startImmediateUpdate(info)
            }
        }
    }

    /** Ask Play whether a newer version is available and, if so, force it. */
    private fun checkForForcedUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                startImmediateUpdate(info)
            }
        }
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
            )
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
