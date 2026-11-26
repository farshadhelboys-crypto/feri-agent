package com.feri.agent.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FeriDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C5CE7),
    secondary = Color(0xFF00CEC9),
    tertiary = Color(0xFFFD79A8),
    background = Color(0xFF0F0F13),
    surface = Color(0xFF1A1A24),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE8E8F0),
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFF252532),
    onSurfaceVariant = Color(0xFFB0B0C0)
)

@Composable
fun FeriAgentTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = FeriDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
