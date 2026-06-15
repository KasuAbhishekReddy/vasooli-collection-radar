package com.vasooli.radar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand — friendly mint → teal (Jupiter/Fi playful style)
val Brand = Color(0xFF14B88A)
val BrandDark = Color(0xFF0D9488)
val Coral = Color(0xFFFF6B5E)   // warm accent for primary actions / Call

// Risk semantics — mint / amber / coral-red
val SafeGreen = Color(0xFF10B981)
val WatchAmber = Color(0xFFF59E0B)
val HighRed = Color(0xFFFB5E54)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFF5E9),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE2DD),
    onSecondaryContainer = Color(0xFF7A211A),
    background = Color(0xFFFAF6EF),       // warm cream
    onBackground = Color(0xFF1C1B19),
    surface = Color.White,
    onSurface = Color(0xFF1C1B19),
    surfaceVariant = Color(0xFFF1ECE2),
    onSurfaceVariant = Color(0xFF7C766C),
    outline = Color(0xFFD8D2C6),
    outlineVariant = Color(0xFFECE6DB),
    error = HighRed,
    onError = Color.White
)

/**
 * One fixed look everywhere — the light/cream "playful" scheme.
 * Deliberately ignores the phone's dark-mode setting so the app always
 * looks identical to the demo video.
 */
@Composable
fun VasooliTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
