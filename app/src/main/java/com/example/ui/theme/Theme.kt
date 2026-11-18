package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val AppDarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = GoldTextOnButton,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = TextPrimary,
    secondary = MintAccent,
    onSecondary = BgDark,
    secondaryContainer = GreenDark,
    onSecondaryContainer = TextPrimary,
    tertiary = GoldLight,
    background = BgDark,
    onBackground = TextPrimary,
    surface = CardDark,
    onSurface = TextPrimary,
    surfaceVariant = Card2Dark,
    onSurfaceVariant = TextMuted,
    outline = LineDark
)

@Composable
fun DilKayAbginayTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = AppDarkColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
