package app.azracelik.harman.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import app.azracelik.harman.ui.components.BudgetRing
import app.azracelik.harman.ui.components.EmptyState
import app.azracelik.harman.ui.components.SoftProgressBar
import app.azracelik.harman.ui.components.TransactionRow
import app.azracelik.harman.ui.components.color

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
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                viewModel.month.label(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                BudgetSummary(summary, onSetLimit = onGoalsClick)
            }
            Spacer(Modifier.height(20.dp))
            GoalCard(state.goal, onClick = onGoalsClick)
            Spacer(Modifier.height(24.dp))
            if (transactions.isNotEmpty()) {
                Text(
                    stringResource(R.string.home_recent),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
            }
        }
        if (state.loaded && transactions.isEmpty()) {
            item {
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
        items(transactions, key = { it.id }) { tx ->
            TransactionRow(tx, onClick = { onEditClick(tx) })
        }
    }
}

@Composable
private fun BudgetSummary(summary: MonthSummary, onSetLimit: () -> Unit) {
    val level = summary.level
    if (!summary.hasLimit) {
        BudgetRing(fraction = 0f, color = level.color()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.ring_no_limit_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatMoney(summary.expenseMinor),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onSetLimit) { Text(stringResource(R.string.ring_set_limit)) }
            }
        }
        return
    }
    BudgetRing(fraction = summary.usedFraction, color = level.color()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                formatMoney(kotlin.math.abs(summary.remainingMinor), showDecimals = false),
                style = MaterialTheme.typography.headlineLarge,
                color = if (level == BudgetLevel.OVER) level.color() else MaterialTheme.colorScheme.onBackground
            )
            Text(
                stringResource(if (level == BudgetLevel.OVER) R.string.ring_over else R.string.ring_left),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(
            R.string.ring_spent_of,
            formatMoney(summary.expenseMinor, showDecimals = false),
            formatMoney(summary.limitMinor, showDecimals = false)
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun GoalCard(goal: SavingsGoal?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Rounded.Savings, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (goal == null) {
                    Text(
                        stringResource(R.string.goal_card_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(goal.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            formatMoney(goal.savedMinor, false) + " / " + formatMoney(goal.targetMinor, false),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SoftProgressBar(
                        fraction = if (goal.targetMinor > 0) goal.savedMinor.toFloat() / goal.targetMinor else 0f,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
