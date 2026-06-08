package com.bluemeanie.axonscanner.util

import androidx.compose.ui.graphics.Color

object ThemeEngine {
    
    // Primary colors
    val DeepSpacePurple = Color(0xFF1A0A2E)
    val Indigo = Color(0xFF2D1B69)
    val ElectricCyan = Color(0xFF00F0FF)
    val CyanGlow = Color(0xFF67E8F9)
    val HotMagenta = Color(0xFFFF006E)
    val MagentaGlow = Color(0xFFFF69B4)
    
    // UI colors
    val Background = Color(0xFF000000)
    val CardBg = Color(0xFF0A0A0A)
    val InputBg = Color(0xFF141414)
    val BorderDefault = Color(0xFF2D2D55)
    val BorderActive = Color(0xFF00F0FF)
    
    // Status colors
    val SuccessGreen = Color(0xFF34D399)
    val WarningYellow = Color(0xFFF6D365)
    val AlertOrange = Color(0xFFFB923C)
    val DangerRed = Color(0xFFF87171)
    val ErrorRed = Color(0xFFEF4444)
    
    // Text colors
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
    
    // Device type colors
    val BodyCamColor = Color(0xFF00F0FF)
    val TaserColor = Color(0xFFF87171)
    val FlexColor = Color(0xFFF6D365)
    val UnknownDevice = Color(0xFF94A3B8)
    
    // Radar colors
    val RadarBackground = Color(0xFF1A1A3A)
    val RadarRing = Color(0xFF2D2D55)
    val RadarSweep = Color(0xFF00F0FF)
    
    data class ThemeColors(
        val name: String,
        val primary: Color,
        val secondary: Color,
        val accent: Color,
        val background: Color,
        val surface: Color,
        val onPrimary: Color,
        val onSurface: Color,
        val glow: Color,
        val border: Color
    )
    
    val themes = mapOf(
        "CLASSIC" to ThemeColors(
            name = "Classic",
            primary = ElectricCyan,
            secondary = HotMagenta,
            accent = CyanGlow,
            background = Background,
            surface = CardBg,
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = ElectricCyan,
            border = BorderDefault
        ),
        "CARBON" to ThemeColors(
            name = "Carbon",
            primary = Color(0xFF00FF88),
            secondary = Color(0xFF00CCFF),
            accent = Color(0xFF00FFAA),
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF111111),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF00FF88),
            border = BorderDefault
        ),
        "TITANIUM" to ThemeColors(
            name = "Titanium",
            primary = Color(0xFFC0C0C0),
            secondary = Color(0xFF808080),
            accent = Color(0xFFE0E0E0),
            background = Color(0xFF050505),
            surface = Color(0xFF1A1A1A),
            onPrimary = Color(0xFF000000),
            onSurface = TextPrimary,
            glow = Color(0xFFC0C0C0),
            border = BorderDefault
        ),
        "AURORA" to ThemeColors(
            name = "Aurora",
            primary = Color(0xFF00FFFF),
            secondary = Color(0xFFFF00FF),
            accent = Color(0xFF00FF88),
            background = Color(0xFF050510),
            surface = Color(0xFF0A0A20),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF00FFFF),
            border = BorderDefault
        ),
        "MONOLITH" to ThemeColors(
            name = "Monolith",
            primary = Color(0xFFFFFFFF),
            secondary = Color(0xFF333333),
            accent = Color(0xFF666666),
            background = Color(0xFF000000),
            surface = Color(0xFF0A0A0A),
            onPrimary = Color(0xFF000000),
            onSurface = TextPrimary,
            glow = Color(0xFFFFFFFF),
            border = BorderDefault
        ),
        "ARCTIC" to ThemeColors(
            name = "Arctic",
            primary = Color(0xFF00BFFF),
            secondary = Color(0xFF87CEEB),
            accent = Color(0xFFADD8E6),
            background = Color(0xFF000510),
            surface = Color(0xFF001020),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF00BFFF),
            border = BorderDefault
        ),
        "MIDNIGHT" to ThemeColors(
            name = "Midnight",
            primary = Color(0xFF4B0082),
            secondary = Color(0xFF8A2BE2),
            accent = Color(0xFF9400D3),
            background = Color(0xFF000000),
            surface = Color(0xFF0A0010),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF8A2BE2),
            border = BorderDefault
        ),
        "QUANTUM" to ThemeColors(
            name = "Quantum",
            primary = Color(0xFF00F0FF),
            secondary = Color(0xFFFF006E),
            accent = Color(0xFFFFD700),
            background = DeepSpacePurple,
            surface = Indigo,
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF00F0FF),
            border = BorderDefault
        ),
        "NOVA" to ThemeColors(
            name = "Nova",
            primary = Color(0xFFFF6600),
            secondary = Color(0xFFFFCC00),
            accent = Color(0xFFFF9900),
            background = Color(0xFF0A0000),
            surface = Color(0xFF150500),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFFFF6600),
            border = BorderDefault
        ),
        "GLASS" to ThemeColors(
            name = "Glass",
            primary = Color(0xFF00F0FF),
            secondary = Color(0xFF00A0FF),
            accent = Color(0xFF80D0FF),
            background = Color(0xFF000000),
            surface = Color(0xFF0A1520),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF00F0FF),
            border = BorderDefault
        ),
        "INFERNO" to ThemeColors(
            name = "Inferno",
            primary = Color(0xFFFF4500),
            secondary = Color(0xFFFFD700),
            accent = Color(0xFFFF6B00),
            background = Color(0xFF0A0000),
            surface = Color(0xFF150500),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFFFF4500),
            border = BorderDefault
        ),
        "SPECTRE" to ThemeColors(
            name = "Spectre",
            primary = Color(0xFF9B59B6),
            secondary = Color(0xFF00F0FF),
            accent = Color(0xFFE91E63),
            background = Color(0xFF050510),
            surface = Color(0xFF0A0A20),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF9B59B6),
            border = BorderDefault
        ),
        "EMBER" to ThemeColors(
            name = "Ember",
            primary = Color(0xFFFF1493),
            secondary = Color(0xFFFF8C00),
            accent = Color(0xFFFF69B4),
            background = Color(0xFF0A0505),
            surface = Color(0xFF150808),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFFFF1493),
            border = BorderDefault
        ),
        "PHANTOM" to ThemeColors(
            name = "Phantom",
            primary = Color(0xFF4B0082),
            secondary = Color(0xFFDC143C),
            accent = Color(0xFF8B008B),
            background = Color(0xFF000000),
            surface = Color(0xFF0A0010),
            onPrimary = TextPrimary,
            onSurface = TextPrimary,
            glow = Color(0xFF4B0082),
            border = BorderDefault
        ),
        "VENOM" to ThemeColors(
            name = "Venom",
            primary = Color(0xFF00FF00),
            secondary = Color(0xFFFF0000),
            accent = Color(0xFFADFF2F),
            background = Color(0xFF000500),
            surface = Color(0xFF000A00),
            onPrimary = Color(0xFF000000),
            onSurface = TextPrimary,
            glow = Color(0xFF00FF00),
            border = BorderDefault
        )
    )
    
    fun getTheme(name: String): ThemeColors {
        return themes[name.uppercase()] ?: themes["CLASSIC"]!!
    }
}