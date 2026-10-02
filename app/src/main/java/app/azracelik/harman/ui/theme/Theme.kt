package app.azracelik.harman.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Material rollerinde karşılığı olmayan, anlamsal renkler. */
@Immutable
data class SemanticColors(
    val income: Color,
    val incomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val warning: Color
)

private val LightSemantic = SemanticColors(
    income = IncomeGreen,
    incomeContainer = IncomeContainer,
    expense = ExpenseTerracotta,
    expenseContainer = ExpenseContainer,
    warning = WarningAmber
)

private val DarkSemantic = SemanticColors(
    income = IncomeGreenDark,
    incomeContainer = IncomeContainerDark,
    expense = ExpenseTerracottaDark,
    expenseContainer = ExpenseContainerDark,
    warning = WarningAmberDark
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemantic }

val MaterialTheme.semantic: SemanticColors
    @Composable @ReadOnlyComposable get() = LocalSemanticColors.current

private val DarkColorScheme = darkColorScheme(
    primary = SageLight,
    onPrimary = Color(0xFF1E281C),
    primaryContainer = Color(0xFF3A4638),
    onPrimaryContainer = Color(0xFFE8EFE6),
    secondary = SageDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3A4638),
    onSecondaryContainer = Color(0xFFE8EFE6),
    tertiary = WarmSand,
    background = Color(0xFF1A1E19),
    onBackground = WarmCream,
    surface = Color(0xFF232822),
    onSurface = WarmCream,
    surfaceVariant = Color(0xFF2E352C),
    onSurfaceVariant = WarmSand,
    outlineVariant = Color(0xFF3F473D)
)

private val LightColorScheme = lightColorScheme(
    primary = SageDark,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = TextDark,
    secondary = SageLight,
    onSecondary = TextDark,
    secondaryContainer = SageContainer,
    onSecondaryContainer = TextDark,
    tertiary = WarmSand,
    onTertiary = TextDark,
    background = WarmCream,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF3EBDF),
    onSurfaceVariant = TextMuted,
    outlineVariant = Color(0xFFE6DDCF)
)

@Composable
fun HarmanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSemanticColors provides if (darkTheme) DarkSemantic else LightSemantic
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
