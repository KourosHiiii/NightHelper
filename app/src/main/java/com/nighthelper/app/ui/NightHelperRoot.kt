package com.nighthelper.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nighthelper.app.MainViewModel
import com.nighthelper.app.Route
import com.nighthelper.app.ui.components.MoonSky
import com.nighthelper.app.ui.screens.FlowScreen
import com.nighthelper.app.ui.screens.GoodNightScreen
import com.nighthelper.app.ui.screens.HistoryScreen
import com.nighthelper.app.ui.screens.SettingsScreen

@Composable
fun NightHelperRoot(startRoute: String) {
    val navController = rememberNavController()
    val vm: MainViewModel = viewModel()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MoonSky(showCornerMoon = false) {
            Scaffold(containerColor = Color.Transparent) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = if (startRoute == Route.SETTINGS) Route.SETTINGS else Route.FLOW,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Route.FLOW) {
                        FlowScreen(
                            vm = vm,
                            onOpenSettings = {
                                navController.navigate(Route.SETTINGS) { launchSingleTop = true }
                            },
                            onGoToGoodNight = {
                                navController.navigate(Route.GOODNIGHT) { launchSingleTop = true }
                            }
                        )
                    }
                    composable(Route.GOODNIGHT) {
                        GoodNightScreen(
                            vm = vm,
                            onOpenHistory = {
                                navController.navigate(Route.HISTORY) { launchSingleTop = true }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(Route.SETTINGS) {
                        SettingsScreen(
                            vm = vm,
                            onBack = { navController.popBackStack() },
                            onOpenHistory = {
                                navController.navigate(Route.HISTORY) { launchSingleTop = true }
                            }
                        )
                    }
                    composable(Route.HISTORY) {
                        HistoryScreen(
                            vm = vm,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
