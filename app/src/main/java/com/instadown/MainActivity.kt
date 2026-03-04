package com.instadown

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.instadown.ui.screens.download.DownloadManagerScreen
import com.instadown.ui.screens.gallery.GalleryScreen
import com.instadown.ui.screens.main.MainScreen
import com.instadown.ui.screens.settings.SettingsScreen
import com.instadown.ui.theme.InstaDownTheme
import com.instadown.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { viewModel.isLoading.value }
        
        enableEdgeToEdge()
        
        handleIntent(intent)

        setContent {
            InstaDownTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InstaDownApp(
                        sharedUrl = viewModel.sharedUrl.value,
                        onUrlProcessed = { viewModel.clearSharedUrl() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                        viewModel.setSharedUrl(text)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                intent.data?.toString()?.let { url ->
                    viewModel.setSharedUrl(url)
                }
            }
        }
    }
}

@Composable
fun InstaDownApp(
    sharedUrl: String?,
    onUrlProcessed: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                sharedUrl = sharedUrl,
                onUrlProcessed = onUrlProcessed
            )
        }
        composable(Screen.Gallery.route) {
            GalleryScreen(navController = navController)
        }
        composable(Screen.Downloads.route) {
            DownloadManagerScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Gallery : Screen("gallery")
    data object Downloads : Screen("downloads")
    data object Settings : Screen("settings")
}
