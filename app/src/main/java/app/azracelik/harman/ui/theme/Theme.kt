package app.azracelik.harman.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SageLight,
    onPrimary = Color(0xFF1E281C),
    primaryContainer = SageDark,
    onPrimaryContainer = Color(0xFFE8EFE6),
    secondary = SageDark,
    onSecondary = Color.White,
    tertiary = WarmSand,
    background = Color(0xFF1A1E19),
    onBackground = WarmCream,
    surface = Color(0xFF232822),
    onSurface = WarmCream,
    surfaceVariant = Color(0xFF383F36),
    onSurfaceVariant = WarmSand
)

private val LightColorScheme = lightColorScheme(
    primary = SageDark,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = TextDark,
    secondary = SageLight,
    onSecondary = TextDark,
    tertiary = WarmSand,
    onTertiary = TextDark,
    background = WarmCream,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = WarmSand.copy(alpha = 0.4f),
    onSurfaceVariant = TextDark
)

@Composable
fun HarmanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Özel marka renklerimizin aktif olması için false
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}