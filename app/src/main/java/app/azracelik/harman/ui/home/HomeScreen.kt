package app.azracelik.harman.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.azracelik.harman.R
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.domain.BudgetLevel
import app.azracelik.harman.domain.MonthSummary
import app.azracelik.harman.domain.formatMoney
import app.azracelik.harman.domain.label
import app.azracelik.harman.ui.HomeViewModel
import app.azracelik.harman.ui.ViewModelFactory
import app.azracelik.harman.ui.components.BentoTile
import app.azracelik.harman.ui.components.EmptyState
import app.azracelik.harman.ui.components.SoftProgressBar
import app.azracelik.harman.ui.components.TransactionRow
import app.azracelik.harman.ui.components.tile
import app.azracelik.harman.ui.theme.Tile
import app.azracelik.harman.ui.theme.semantic
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onEditClick: (Transaction) -> Unit,
    onGoalsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val summary = state.month.summary
    val transactions = state.month.transactions

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Header(viewModel.month.label(), onAddClick) }
        item {
            Spacer(Modifier.height(8.dp))
            HeroTile(summary, onSetLimit = onGoalsClick)
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowTile(
                    stringResource(R.string.income), summary.incomeMinor,
                    Icons.Rounded.ArrowUpward, MaterialTheme.semantic.lilac, Modifier.weight(1f)
                )
                FlowTile(
                    stringResource(R.string.expense), summary.expenseMinor,
                    Icons.Rounded.ArrowDownward, MaterialTheme.semantic.peach, Modifier.weight(1f)
                )
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            GoalTile(
                state.goal,
                // Hero sarıyken hedef kutusu aynı renkte olmasın
                tile = if (summary.level == BudgetLevel.WARNING) MaterialTheme.semantic.mint
                else MaterialTheme.semantic.butter,
                onClick = onGoalsClick
            )
        }
        item {
            Text(
                stringResource(R.string.home_recent),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 6.dp, top = 18.dp, bottom = 4.dp)
            )
        }
        if (state.loaded && transactions.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Receipt,
                        title = stringResource(R.string.home_empty_title),
                        body = stringResource(R.string.home_empty_body),
                        action = {
                            Button(onClick = onAddClick) { Text(stringResource(R.string.home_empty_cta)) }
                        }
                    )
                }
            }
        }
        items(transactions, key = { it.id }) { tx ->
            TransactionRow(tx, onClick = { onEditClick(tx) })
        }
    }
}

@Composable
private fun Header(monthLabel: String, onAddClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                monthLabel,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = stringResource(R.string.add_transaction),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/** Ana kutu: duruma göre mint → sarı → şeftali; limit yoksa lila ve harcanan tutarı gösterir. */
@Composable
private fun HeroTile(summary: MonthSummary, onSetLimit: () -> Unit) {
    val level = summary.level
    val tile = level.tile()
    val onBg = MaterialTheme.colorScheme.onBackground
    BentoTile(tile, Modifier.fillMaxWidth(), contentPadding = 22.dp) {
        if (!summary.hasLimit) {
            Text(
                stringResource(R.string.hero_spent_title),
                style = MaterialTheme.typography.titleMedium,
                color = tile.content
            )
            Text(
                formatMoney(summary.expenseMinor, showDecimals = false),
                style = MaterialTheme.typography.displayLarge,
                color = onBg,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
            )
            Button(onClick = onSetLimit) { Text(stringResource(R.string.hero_set_limit)) }
            return@BentoTile
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (level == BudgetLevel.OVER) R.string.hero_over else R.string.hero_left),
                style = MaterialTheme.typography.titleMedium,
                color = tile.content,
                modifier = Modifier.weight(1f)
            )
            Pill(
                stringResource(R.string.hero_pct, (summary.expenseMinor * 100f / summary.limitMinor).roundToInt()),
                tile
            )
        }
        Text(
            formatMoney(abs(summary.remainingMinor), showDecimals = false),
            style = MaterialTheme.typography.displayLarge,
            color = onBg,
            modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
        )
        SoftProgressBar(
            fraction = summary.usedFraction,
            color = MaterialTheme.colorScheme.primary,
            track = tile.content.copy(alpha = 0.16f),
            height = 14.dp
        )
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.hero_spent, formatMoney(summary.expenseMinor, showDecimals = false)),
                style = MaterialTheme.typography.bodyMedium,
                color = tile.content
            )
            Text(
                stringResource(R.string.hero_limit, formatMoney(summary.limitMinor, showDecimals = false)),
                style = MaterialTheme.typography.bodyMedium,
                color = tile.content
            )
        }
    }
}

@Composable
private fun Pill(text: String, tile: Tile) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = tile.content,
        modifier = Modifier
            .clip(CircleShape)
            .background(tile.content.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
private fun FlowTile(label: String, amountMinor: Long, icon: ImageVector, tile: Tile, modifier: Modifier) {
    BentoTile(tile, modifier.height(108.dp), contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(tile.content.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, Modifier.size(15.dp), tint = tile.content) }
            Text(label, style = MaterialTheme.typography.titleSmall, color = tile.content)
        }
        Spacer(Modifier.weight(1f))
        Text(
            formatMoney(amountMinor, showDecimals = false),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun GoalTile(goal: SavingsGoal?, tile: Tile, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(tile.container)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(tile.content.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Flag, null, Modifier.size(24.dp), tint = tile.content) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (goal == null) {
                Text(
                    stringResource(R.string.goal_card_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text(
                        goal.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        formatMoney(goal.savedMinor, false) + " / " + formatMoney(goal.targetMinor, false),
                        style = MaterialTheme.typography.labelLarge,
                        color = tile.content
                    )
                }
                SoftProgressBar(
                    fraction = if (goal.targetMinor > 0) goal.savedMinor.toFloat() / goal.targetMinor else 0f,
                    color = MaterialTheme.colorScheme.primary,
                    track = tile.content.copy(alpha = 0.16f)
                )
            }
        }
    }
}
