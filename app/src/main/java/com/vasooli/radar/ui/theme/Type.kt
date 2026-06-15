package com.vasooli.radar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.vasooli.radar.R

val Jakarta = FontFamily(
    Font(R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(R.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold),
    Font(R.font.plusjakartasans_extrabold, FontWeight.ExtraBold)
)

private val base = Typography()

val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold),
    displayMedium = base.displayMedium.copy(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold),
    displaySmall = base.displaySmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold),
    headlineLarge = base.headlineLarge.copy(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Jakarta),
    bodyMedium = base.bodyMedium.copy(fontFamily = Jakarta),
    bodySmall = base.bodySmall.copy(fontFamily = Jakarta),
    labelLarge = base.labelLarge.copy(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = Jakarta, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.Medium)
)
