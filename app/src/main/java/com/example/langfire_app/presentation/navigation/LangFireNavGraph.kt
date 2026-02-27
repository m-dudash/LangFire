package com.example.langfire_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.langfire_app.presentation.screens.*

@Composable
fun LangFireNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onLibraryClick = { navController.navigate(Screen.Library.route) },
                onBurnClick = { navController.navigate(Screen.Session.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onFortuneClick = { navController.navigate(Screen.FortuneWheel.route) }
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen()
        }
        composable(Screen.Session.route) {
            SessionScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onLibraryClick = { navController.navigate(Screen.Library.route) },
                onBurnClick    = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }
        composable(Screen.FortuneWheel.route) {
            FortuneWheelScreen()
        }
    }
}
