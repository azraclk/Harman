package app.azracelik.harman.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.harman.data.model.Category
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.data.model.TransactionType
import app.azracelik.harman.domain.BudgetLevel
import app.azracelik.harman.domain.formatMoney
import app.azracelik.harman.domain.shortLabel
import app.azracelik.harman.ui.theme.Tile
import app.azracelik.harman.ui.theme.semantic
import java.time.LocalDate

fun Category.icon(): ImageVector = when (this) {
    Category.MARKET -> Icons.Rounded.ShoppingCart
    Category.BILLS -> Icons.Rounded.Receipt
    Category.TRANSPORT -> Icons.Rounded.DirectionsBus
    Category.SHOPPING -> Icons.Rounded.ShoppingBag
    Category.FUN -> Icons.Rounded.TheaterComedy
    Category.HEALTH -> Icons.Rounded.Favorite
    Category.EDUCATION -> Icons.Rounded.School
    Category.OTHER_EXPENSE -> Icons.Rounded.MoreHoriz
    Category.SALARY -> Icons.Rounded.Payments
    Category.EXTRA_INCOME -> Icons.Rounded.AttachMoney
}

/** Her kategoriye sabit bir pastel ton: liste göze çarpan ama tutarlı kalır. */
@Composable
fun Category.tile(): Tile {
    val s = MaterialTheme.semantic
    return when (this) {
        Category.MARKET, Category.OTHER_EXPENSE -> s.mint
        Category.BILLS, Category.EDUCATION -> s.butter
        Category.TRANSPORT, Category.FUN, Category.SALARY, Category.EXTRA_INCOME -> s.lilac
        Category.SHOPPING, Category.HEALTH -> s.peach
    }
}

/** Bütçe durumuna göre ana kutunun tonu: yeşil → sarı → şeftali. */
@Composable
fun BudgetLevel.tile(): Tile = when (this) {
    BudgetLevel.NO_LIMIT -> MaterialTheme.semantic.lilac
    BudgetLevel.OK -> MaterialTheme.semantic.mint
    BudgetLevel.WARNING -> MaterialTheme.semantic.butter
    BudgetLevel.OVER -> MaterialTheme.semantic.peach
}

/** Yuvarlak köşeli, pastel bento kutusu. */
@Composable
fun BentoTile(
    tile: Tile,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(tile.container)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SoftProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    track: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 10.dp
) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(700), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun CategoryBadge(category: Category, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val tile = category.tile()
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(tile.container),
        contentAlignment = Alignment.Center
    ) {
        Icon(category.icon(), contentDescription = null, tint = tile.content, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun TransactionRow(transaction: Transaction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) MaterialTheme.semantic.income else MaterialTheme.colorScheme.onSurface
    val subtitle = LocalDate.ofEpochDay(transaction.epochDay).shortLabel() +
        if (transaction.note.isNotBlank()) " · " + transaction.note else ""
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CategoryBadge(transaction.category, size = 42.dp)
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(transaction.category.labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            (if (isIncome) "+" else "−") + formatMoney(transaction.amountMinor),
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.semantic.lilac.container),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(40.dp), tint = MaterialTheme.semantic.lilac.content)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        if (body.isNotBlank()) {
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (action != null) action()
    }
}
