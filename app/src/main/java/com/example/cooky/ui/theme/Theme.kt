package com.example.cooky.ui.theme

import android.app.Activity
import android.os.Build
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

private val DarkColorScheme = darkColorScheme(
    primary = MintDark,
    onPrimary = Color(0xFF00382E),
    primaryContainer = MintDarkDim,
    onPrimaryContainer = Color(0xFF00201B),
    secondary = CoralDark,
    onSecondary = Color(0xFF4A2800),
    tertiary = SkyDark,
    onTertiary = Color(0xFF003258),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF3F3D3B),
    onSurfaceVariant = Color(0xFFD6D3D1),
    outline = Color(0xFFA8A29E),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D)
)

private val LightColorScheme = lightColorScheme(
    primary = Mint,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = Color(0xFF00201B),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = CoralLight,
    onSecondaryContainer = Color(0xFF4A2800),
    tertiary = Sky,
    onTertiary = Color.White,
    tertiaryContainer = SkyLight,
    onTertiaryContainer = Color(0xFF003258),
    background = Cream,
    onBackground = WarmGrayDark,
    surface = WarmWhite,
    onSurface = WarmGrayDark,
    surfaceVariant = SurfaceVariantFresh,
    onSurfaceVariant = WarmGray,
    outline = OutlineFresh,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun CookyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
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
