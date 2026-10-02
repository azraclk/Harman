package app.azracelik.harman.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import app.azracelik.harman.ui.theme.semantic

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

@Composable
fun BudgetLevel.color(): Color = when (this) {
    BudgetLevel.NO_LIMIT, BudgetLevel.OK -> MaterialTheme.colorScheme.primary
    BudgetLevel.WARNING -> MaterialTheme.semantic.warning
    BudgetLevel.OVER -> MaterialTheme.semantic.expense
}

/** Limit kullanımını gösteren animasyonlu halka; ortasına içerik koyulur. */
@Composable
fun BudgetRing(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 232.dp,
    strokeWidth: Dp = 18.dp,
    content: @Composable () -> Unit
) {
    val animated by animateFloatAsState(fraction, tween(900), label = "ring")
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            if (animated > 0f) {
                drawArc(
                    color, -90f, 360f * animated, false, topLeft, arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

@Composable
fun SoftProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(700), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
    val semantic = MaterialTheme.semantic
    val (bg, fg) = if (category.type == TransactionType.INCOME)
        semantic.incomeContainer to semantic.income
    else MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(category.icon(), contentDescription = null, tint = fg, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun TransactionRow(transaction: Transaction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) MaterialTheme.semantic.income else MaterialTheme.colorScheme.onSurface
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CategoryBadge(transaction.category)
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(transaction.category.labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (transaction.note.isNotBlank()) {
                Text(
                    transaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) action()
    }
}
