package app.azracelik.harman.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.azracelik.harman.ui.components.BentoTile
import app.azracelik.harman.ui.components.CategoryBadge
import app.azracelik.harman.ui.components.EmptyState
import app.azracelik.harman.ui.components.SoftProgressBar
import app.azracelik.harman.ui.components.tile
import app.azracelik.harman.ui.theme.Tile
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
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
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
                    style = MaterialTheme.typography.headlineLarge
                )
                IconButton(onClick = viewModel::next, enabled = viewModel.canGoNext(state.month)) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, stringResource(R.string.report_next_month))
                }
            }
        }
        if (summary != null) {
            item { BalanceTile(summary) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AmountTile(
                        stringResource(R.string.income), summary.incomeMinor,
                        MaterialTheme.semantic.mint, Modifier.weight(1f)
                    )
                    AmountTile(
                        stringResource(R.string.expense), summary.expenseMinor,
                        MaterialTheme.semantic.peach, Modifier.weight(1f)
                    )
                }
            }
            if (summary.expenseByCategory.isEmpty()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        EmptyState(Icons.Rounded.PieChart, stringResource(R.string.report_empty), "")
                    }
                }
            } else {
                item {
                    Text(
                        stringResource(R.string.report_categories),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 6.dp, top = 8.dp)
                    )
                }
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        summary.expenseByCategory.forEach { CategoryRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceTile(summary: MonthSummary) {
    val tile = MaterialTheme.semantic.lilac
    BentoTile(tile, Modifier.fillMaxWidth(), contentPadding = 22.dp) {
        Text(stringResource(R.string.report_balance), style = MaterialTheme.typography.titleMedium, color = tile.content)
        Text(
            formatMoney(summary.balanceMinor),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AmountTile(label: String, amountMinor: Long, tile: Tile, modifier: Modifier) {
    BentoTile(tile, modifier.height(100.dp), contentPadding = 16.dp) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = tile.content)
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        Text(
            formatMoney(amountMinor),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CategoryRow(item: CategoryTotal) {
    val tile = item.category.tile()
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CategoryBadge(item.category)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(item.category.labelRes), style = MaterialTheme.typography.titleMedium)
                Text(formatMoney(item.totalMinor), style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SoftProgressBar(
                    item.share, tile.content, Modifier.weight(1f),
                    track = tile.container, height = 8.dp
                )
                Text(
                    "%${(item.share * 100).roundToInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
