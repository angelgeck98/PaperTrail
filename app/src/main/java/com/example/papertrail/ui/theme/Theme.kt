package com.example.papertrail.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// 🌿 Custom green light theme
private val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF3D9970),           // PapertrailGreen
    onPrimary = Color.White,
    secondary = Color(0xFFB9FBC0),         // Light accent green
    onSecondary = Color.Black,
    background = Color(0xFFF0FDF4),        // Light background
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFF0FDF4),
    onSurface = Color(0xFF1B1B1B),
)

// 🌙 Optional dark theme fallback
private val GreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF2E7D5F),
    onPrimary = Color.White,
    secondary = Color(0xFF87CBB9),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
)

@Composable
fun PaperTrailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> GreenDarkColorScheme
        else -> GreenLightColorScheme
    }

    // Status bar theming
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // ✅ uses Inter
        content = content
    )
}
