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

/** Bir bento kutusunun zemini ve üzerindeki metin/ikon rengi. */
@Immutable
data class Tile(val container: Color, val content: Color)

/** Material rollerinde karşılığı olmayan, anlamsal renkler ve pastel kutular. */
@Immutable
data class SemanticColors(
    val income: Color,
    val expense: Color,
    val warning: Color,
    val mint: Tile,
    val lilac: Tile,
    val peach: Tile,
    val butter: Tile
)

private val LightSemantic = SemanticColors(
    income = IncomeText,
    expense = ExpenseText,
    warning = WarningText,
    mint = Tile(MintTile, OnMint),
    lilac = Tile(LilacTile, OnLilac),
    peach = Tile(PeachTile, OnPeach),
    butter = Tile(ButterTile, OnButter)
)

private val DarkSemantic = SemanticColors(
    income = IncomeTextDark,
    expense = ExpenseTextDark,
    warning = WarningTextDark,
    mint = Tile(MintTileDark, OnMintDark),
    lilac = Tile(LilacTileDark, OnLilacDark),
    peach = Tile(PeachTileDark, OnPeachDark),
    butter = Tile(ButterTileDark, OnButterDark)
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemantic }

val MaterialTheme.semantic: SemanticColors
    @Composable @ReadOnlyComposable get() = LocalSemanticColors.current

private val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = LilacTile,
    onPrimaryContainer = OnLilac,
    secondary = OnLilac,
    onSecondary = Color.White,
    secondaryContainer = LilacTile,
    onSecondaryContainer = Color(0xFF2B2260),
    tertiary = ButterTile,
    onTertiary = OnButter,
    background = BentoBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = BentoTrack,
    onSurfaceVariant = BentoMuted,
    outline = Color(0xFF8D89B3),
    outlineVariant = BentoOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = InkDark,
    onPrimary = Ink,
    primaryContainer = LilacTileDark,
    onPrimaryContainer = OnLilacDark,
    secondary = OnLilacDark,
    onSecondary = Ink,
    secondaryContainer = LilacTileDark,
    onSecondaryContainer = OnLilacDark,
    tertiary = ButterTileDark,
    onTertiary = OnButterDark,
    background = BentoBackgroundDark,
    onBackground = InkDark,
    surface = BentoSurfaceDark,
    onSurface = InkDark,
    surfaceVariant = BentoTrackDark,
    onSurfaceVariant = BentoMutedDark,
    outline = Color(0xFF6C6894),
    outlineVariant = BentoOutlineDark
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
