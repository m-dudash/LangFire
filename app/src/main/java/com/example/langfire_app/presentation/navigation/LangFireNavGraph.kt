package com.example.langfire_app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.langfire_app.presentation.screens.*
import com.example.langfire_app.presentation.ui.theme.FireOrange
import com.example.langfire_app.presentation.viewmodels.MainViewModel

@Composable
fun LangFireNavGraph(navController: NavHostController) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()

    if (mainState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FireOrange)
        }
        return
    }

    val startRoute = if (mainState.hasProfile) Screen.Home.route else Screen.Registration.route

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Screen.Registration.route) {
            RegistrationScreen(
                onRegistrationSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Registration.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) { backStackEntry ->
            val streakJustUpdated = backStackEntry.savedStateHandle.get<Boolean>("streakJustUpdated") == true
            backStackEntry.savedStateHandle.remove<Boolean>("streakJustUpdated")

            HomeScreen(
                streakJustUpdated = streakJustUpdated,
                onLibraryClick = { navController.navigate(Screen.Library.route) },
                onBurnClick = { navController.navigate(Screen.Session.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onFortuneClick = { navController.navigate(Screen.FortuneWheel.route) }
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                onBurnClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onLibraryClick = { navController.navigate(Screen.Library.route) },
                onUnitClick = { unitId -> navController.navigate(Screen.UnitDetails.createRoute(unitId)) }
            )
        }
        composable(
            route = Screen.UnitDetails.route,
            arguments = listOf(androidx.navigation.navArgument("unitId") { type = androidx.navigation.NavType.IntType })
        ) {
            UnitDetailsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Session.route) {
            SessionScreen(
                onFinishClick = { streakJustUpdated ->
                    navController.getBackStackEntry(Screen.Home.route).savedStateHandle["streakJustUpdated"] = streakJustUpdated
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
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
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onAchievementsClick = { navController.navigate(Screen.Achievements.route) }
            )
        }
        composable(Screen.Achievements.route) {
            AchievementsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.FortuneWheel.route) {
            FortuneScreen(
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
    }
}
