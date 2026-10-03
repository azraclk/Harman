package app.azracelik.harman.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.azracelik.harman.R

/** Outfit (SIL OFL) uygulamaya gömülü değişken font; ağ/Play Services gerektirmez. */
@OptIn(ExperimentalTextApi::class)
val AppFontFamily = FontFamily(
    listOf(
        FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold
    ).map { weight ->
        Font(
            R.font.outfit_variable,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
        )
    }
)

private fun style(weight: FontWeight, size: Int, line: Int, spacing: Double = 0.0) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp
)

val Typography = Typography(
    displayLarge = style(FontWeight.ExtraBold, 50, 56, -1.0),
    displayMedium = style(FontWeight.Bold, 40, 46, -0.8),
    headlineLarge = style(FontWeight.Bold, 28, 34, -0.4),
    headlineMedium = style(FontWeight.SemiBold, 22, 28),
    headlineSmall = style(FontWeight.SemiBold, 18, 24),
    titleLarge = style(FontWeight.Bold, 19, 26),
    titleMedium = style(FontWeight.SemiBold, 16, 22),
    titleSmall = style(FontWeight.SemiBold, 14, 20),
    bodyLarge = style(FontWeight.Normal, 16, 24),
    bodyMedium = style(FontWeight.Normal, 14, 20),
    bodySmall = style(FontWeight.Normal, 12, 16),
    labelLarge = style(FontWeight.SemiBold, 14, 20),
    labelMedium = style(FontWeight.Medium, 12, 16),
    labelSmall = style(FontWeight.Medium, 11, 16)
)
