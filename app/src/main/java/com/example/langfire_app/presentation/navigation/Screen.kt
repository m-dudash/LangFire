package com.example.langfire_app.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Session : Screen("session")
    object Profile : Screen("profile")
    object FortuneWheel : Screen("fortune_wheel")
}
