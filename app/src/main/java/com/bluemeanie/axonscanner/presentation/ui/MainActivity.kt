package com.bluemeanie.axonscanner.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bluemeanie.axonscanner.presentation.ui.screens.radar.RadarScreen
import com.bluemeanie.axonscanner.presentation.ui.screens.feed.FeedScreen
import com.bluemeanie.axonscanner.presentation.ui.screens.heatmap.HeatmapScreen
import com.bluemeanie.axonscanner.presentation.ui.screens.intel.IntelScreen
import com.bluemeanie.axonscanner.presentation.ui.screens.gear.GearScreen
import com.bluemeanie.axonscanner.presentation.ui.screens.onboarding.OnboardingScreen
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieTheme
import com.bluemeanie.axonscanner.presentation.viewmodel.RadarViewModel
import com.bluemeanie.axonscanner.presentation.viewmodel.SettingsViewModel
import com.bluemeanie.axonscanner.util.ThemeEngine
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requiredPermissions = buildList {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions granted, continue to app
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()
            val onboardingCompleted by settingsViewModel.settings.collectAsState()

            BlueMeanieTheme(themeName = settings.theme.name) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ThemeEngine.Background
                ) {
                    MainNavigation(
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
}

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Radar : Screen("radar")
    object Feed : Screen("feed")
    object Heatmap : Screen("heatmap")
    object Intel : Screen("intel")
    object Gear : Screen("gear")
}

@Composable
fun MainNavigation(
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val settings by settingsViewModel.settings.collectAsState()
    val onboardingCompleted by settingsViewModel.settings.collectAsState()

    val startDestination = if (settings.theme != settings.theme) {
        Screen.Onboarding.route
    } else {
        Screen.Radar.route
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Radar.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Radar.route) {
            RadarScreen(
                onNavigateToFeed = { navController.navigate(Screen.Feed.route) },
                onNavigateToHeatmap = { navController.navigate(Screen.Heatmap.route) },
                onNavigateToIntel = { navController.navigate(Screen.Intel.route) },
                onNavigateToGear = { navController.navigate(Screen.Gear.route) }
            )
        }

        composable(Screen.Feed.route) {
            FeedScreen(
                onNavigateToRadar = { navController.navigate(Screen.Radar.route) },
                onNavigateToHeatmap = { navController.navigate(Screen.Heatmap.route) },
                onNavigateToIntel = { navController.navigate(Screen.Intel.route) },
                onNavigateToGear = { navController.navigate(Screen.Gear.route) }
            )
        }

        composable(Screen.Heatmap.route) {
            HeatmapScreen(
                onNavigateToRadar = { navController.navigate(Screen.Radar.route) },
                onNavigateToFeed = { navController.navigate(Screen.Feed.route) },
                onNavigateToIntel = { navController.navigate(Screen.Intel.route) },
                onNavigateToGear = { navController.navigate(Screen.Gear.route) }
            )
        }

        composable(Screen.Intel.route) {
            IntelScreen(
                onNavigateToRadar = { navController.navigate(Screen.Radar.route) },
                onNavigateToFeed = { navController.navigate(Screen.Feed.route) },
                onNavigateToHeatmap = { navController.navigate(Screen.Heatmap.route) },
                onNavigateToGear = { navController.navigate(Screen.Gear.route) }
            )
        }

        composable(Screen.Gear.route) {
            GearScreen(
                onNavigateToRadar = { navController.navigate(Screen.Radar.route) },
                onNavigateToFeed = { navController.navigate(Screen.Feed.route) },
                onNavigateToHeatmap = { navController.navigate(Screen.Heatmap.route) },
                onNavigateToIntel = { navController.navigate(Screen.Intel.route) }
            )
        }
    }
}