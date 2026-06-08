package com.bluemeanie.axonscanner.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.bluemeanie.axonscanner.util.ThemeEngine

private val DarkColorScheme = darkColorScheme(
    primary = ThemeEngine.ElectricCyan,
    secondary = ThemeEngine.HotMagenta,
    tertiary = ThemeEngine.CyanGlow,
    background = ThemeEngine.Background,
    surface = ThemeEngine.CardBg,
    onPrimary = ThemeEngine.TextPrimary,
    onSecondary = ThemeEngine.TextPrimary,
    onTertiary = ThemeEngine.TextPrimary,
    onBackground = ThemeEngine.TextPrimary,
    onSurface = ThemeEngine.TextPrimary,
    error = ThemeEngine.DangerRed,
    onError = ThemeEngine.TextPrimary
)

data class BlueMeanieColors(
    val primary: androidx.compose.ui.graphics.Color,
    val secondary: androidx.compose.ui.graphics.Color,
    val accent: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
    val surface: androidx.compose.ui.graphics.Color,
    val glow: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textMuted: androidx.compose.ui.graphics.Color,
    val success: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val danger: androidx.compose.ui.graphics.Color,
    val bodyCam: androidx.compose.ui.graphics.Color,
    val taser: androidx.compose.ui.graphics.Color,
    val flex: androidx.compose.ui.graphics.Color
)

val LocalBlueMeanieColors = staticCompositionLocalOf {
    BlueMeanieColors(
        primary = ThemeEngine.ElectricCyan,
        secondary = ThemeEngine.HotMagenta,
        accent = ThemeEngine.CyanGlow,
        background = ThemeEngine.Background,
        surface = ThemeEngine.CardBg,
        glow = ThemeEngine.ElectricCyan,
        textPrimary = ThemeEngine.TextPrimary,
        textSecondary = ThemeEngine.TextSecondary,
        textMuted = ThemeEngine.TextMuted,
        success = ThemeEngine.SuccessGreen,
        warning = ThemeEngine.WarningYellow,
        danger = ThemeEngine.DangerRed,
        bodyCam = ThemeEngine.BodyCamColor,
        taser = ThemeEngine.TaserColor,
        flex = ThemeEngine.FlexColor
    )
}

@Composable
fun BlueMeanieTheme(
    themeName: String = "CLASSIC",
    content: @Composable () -> Unit
) {
    val themeColors = ThemeEngine.getTheme(themeName)
    
    val blueMeanieColors = BlueMeanieColors(
        primary = themeColors.primary,
        secondary = themeColors.secondary,
        accent = themeColors.accent,
        background = themeColors.background,
        surface = themeColors.surface,
        glow = themeColors.glow,
        textPrimary = ThemeEngine.TextPrimary,
        textSecondary = ThemeEngine.TextSecondary,
        textMuted = ThemeEngine.TextMuted,
        success = ThemeEngine.SuccessGreen,
        warning = ThemeEngine.WarningYellow,
        danger = ThemeEngine.DangerRed,
        bodyCam = ThemeEngine.BodyCamColor,
        taser = ThemeEngine.TaserColor,
        flex = ThemeEngine.FlexColor
    )

    CompositionLocalProvider(LocalBlueMeanieColors provides blueMeanieColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }
}

object BlueMeanieTheme {
    val colors: BlueMeanieColors
        @Composable
        get() = LocalBlueMeanieColors.current
}