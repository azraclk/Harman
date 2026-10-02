package app.azracelik.harman.ui.report

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import app.azracelik.harman.domain.CategoryTotal
import app.azracelik.harman.domain.MonthSummary
import app.azracelik.harman.domain.formatMoney
import app.azracelik.harman.domain.label
import app.azracelik.harman.ui.ReportViewModel
import app.azracelik.harman.ui.ViewModelFactory
import app.azracelik.harman.ui.components.CategoryBadge
import app.azracelik.harman.ui.components.EmptyState
import app.azracelik.harman.ui.components.SoftProgressBar
import app.azracelik.harman.ui.theme.semantic
import kotlin.math.roundToInt

@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(factory = ViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val summary = state.data?.summary

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::previous) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, stringResource(R.string.report_prev_month))
                }
                Text(
                    state.month.label(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = viewModel::next, enabled = viewModel.canGoNext(state.month)) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, stringResource(R.string.report_next_month))
                }
            }
        }
        if (summary != null) {
            item { TotalsCard(summary) }
            if (summary.expenseByCategory.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Rounded.PieChart,
                        stringResource(R.string.report_empty),
                        ""
                    )
                }
            } else {
                item {
                    Text(
                        stringResource(R.string.report_categories),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(summary.expenseByCategory, key = { it.category }) { CategoryRow(it) }
            }
        }
    }
}

@Composable
private fun TotalsCard(summary: MonthSummary) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(
                    stringResource(R.string.report_balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatMoney(summary.balanceMinor),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (summary.balanceMinor < 0) MaterialTheme.semantic.expense
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat(
                    stringResource(R.string.income), formatMoney(summary.incomeMinor),
                    MaterialTheme.semantic.income, MaterialTheme.semantic.incomeContainer, Modifier.weight(1f)
                )
                Stat(
                    stringResource(R.string.expense), formatMoney(summary.expenseMinor),
                    MaterialTheme.semantic.expense, MaterialTheme.semantic.expenseContainer, Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Stat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color,
    modifier: Modifier
) {
    Column(
        modifier
            .background(container, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun CategoryRow(item: CategoryTotal) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CategoryBadge(item.category)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(item.category.labelRes), style = MaterialTheme.typography.titleMedium)
                Text(formatMoney(item.totalMinor), style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SoftProgressBar(item.share, MaterialTheme.colorScheme.primary, Modifier.weight(1f), height = 8.dp)
                Text(
                    "%${(item.share * 100).roundToInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(Modifier.height(0.dp))
}
