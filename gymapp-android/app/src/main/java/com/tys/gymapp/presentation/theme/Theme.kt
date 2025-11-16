package com.tys.gymapp.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark Color Scheme - Màu cho Dark Mode
 */
private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryVariantDark,
    onPrimaryContainer = Color.White,

    // Secondary
    secondary = SecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = SecondaryVariantDark,
    onSecondaryContainer = Color.White,

    // Tertiary
    tertiary = WarningDark,
    onTertiary = Color.Black,

    // Background
    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    // Surface
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,

    // Error
    error = ErrorDark,
    onError = Color.White,

    // Outline
    outline = CardBorderDark,
    outlineVariant = Color(0xFF4A4A4A)
)

/**
 * Light Color Scheme - Màu cho Light Mode
 */
private val LightColorScheme = lightColorScheme(
    // Primary
    primary = Primary,
    onPrimary = TextLight,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = TextLight,

    // Secondary
    secondary = Secondary,
    onSecondary = TextLight,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = TextLight,

    // Tertiary
    tertiary = Warning,
    onTertiary = TextPrimary,

    // Background
    background = BackgroundLight,
    onBackground = TextPrimary,

    // Surface
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = TextSecondary,

    // Error
    error = Error,
    onError = TextLight,

    // Outline
    outline = CardBorder,
    outlineVariant = Color(0xFFDDDDDD)
)

/**
 * GymAppTheme - Theme chính của app
 * @param darkTheme - true nếu dùng Dark Mode
 * @param dynamicColor - true nếu dùng Material You dynamic colors (Android 12+)
 */
@Composable
fun GymAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Tắt dynamic color để dùng màu custom
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context)
        //     else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Set status bar color
            window.statusBarColor = colorScheme.primary.toArgb()

            // Set navigation bar color
            window.navigationBarColor = colorScheme.surface.toArgb()

            // Set status bar icons color (dark/light)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}