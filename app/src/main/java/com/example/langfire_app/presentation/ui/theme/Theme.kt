package com.example.langfire_app.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LangFireLightColors = lightColorScheme(
    primary            = FireOrange,
    onPrimary          = androidx.compose.ui.graphics.Color.White,
    primaryContainer   = FireContainer,
    onPrimaryContainer = OnFireContainer,
    secondary          = GoldXP,
    onSecondary        = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnFireContainer,
    tertiary           = FortunePurple,
    onTertiary         = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer  = FortuneContainer,
    background         = Background,
    onBackground       = OnSurface,
    surface            = Surface,
    onSurface          = OnSurface,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = OnSurfaceVariant,
    outline            = Outline
)

@Composable
fun LangFireappTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            window.navigationBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = LangFireLightColors,
        typography  = Typography,
        content     = content
    )
}