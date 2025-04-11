package com.example.papertrail.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PastelLightColorScheme = lightColorScheme(
    primary = Color(0xFFB5E6D1), // Pastel Mint
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFFD1F0E5),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFFF5C3C2), // Pastel Pink
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFFFFE0E0),
    onSecondaryContainer = Color(0xFF1A1A1A),
    tertiary = Color(0xFFB8D8F0), // Pastel Blue
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFFD1E8F8),
    onTertiaryContainer = Color(0xFF1A1A1A),
    background = Color(0xFFFDFDFD),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFDFDFD),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF1A1A1A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF1A1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF1A1A1A)
)

private val PastelDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4A8C6D), // Darker Pastel Mint
    onPrimary = Color(0xFFF0F0F0),
    primaryContainer = Color(0xFF2D5A45),
    onPrimaryContainer = Color(0xFFF0F0F0),
    secondary = Color(0xFFC98A89), // Darker Pastel Pink
    onSecondary = Color(0xFFF0F0F0),
    secondaryContainer = Color(0xFF8C5F5E),
    onSecondaryContainer = Color(0xFFF0F0F0),
    tertiary = Color(0xFF6B8CA4), // Darker Pastel Blue
    onTertiary = Color(0xFFF0F0F0),
    tertiaryContainer = Color(0xFF4A5F70),
    onTertiaryContainer = Color(0xFFF0F0F0),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFF0F0F0),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF0F0F0),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFF0F0F0),
    error = Color(0xFF8C4A45),
    onError = Color(0xFFF0F0F0),
    errorContainer = Color(0xFF5A2D2A),
    onErrorContainer = Color(0xFFF0F0F0)
)

@Composable
fun PaperTrailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PastelDarkColorScheme
        else -> PastelLightColorScheme
    }
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
        typography = Typography,
        content = content
    )
}