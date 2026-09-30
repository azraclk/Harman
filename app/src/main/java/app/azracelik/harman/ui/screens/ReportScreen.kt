package app.azracelik.harman.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.harman.data.Transaction
import app.azracelik.harman.ui.theme.getCategoryIcon
import app.azracelik.harman.viewmodel.TransactionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    val totalIncome = transactions
        .filter { it.type == "GELIR" || it.type == "INCOME" }
        .sumOf { it.amount }
    val totalExpense = transactions
        .filter { it.type == "GIDER" || it.type == "EXPENSE" }
        .sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    var selectedFilter by remember { mutableStateOf("GIDER") } // "GIDER" or "GELIR"

    val filteredTransactions = transactions.filter {
        if (selectedFilter == "GIDER") {
            it.type == "GIDER" || it.type == "EXPENSE"
        } else {
            it.type == "GELIR" || it.type == "INCOME"
        }
    }

    val totalFilteredAmount = filteredTransactions.sumOf { it.amount }

    // Group transactions by category name
    val categorySummaryList = filteredTransactions
        .groupBy { it.categoryName.ifBlank { "Diğer" } }
        .map { (category, list) ->
            val total = list.sumOf { it.amount }
            val percentage = if (totalFilteredAmount > 0) (total / totalFilteredAmount) else 0.0
            CategorySummary(
                categoryName = category,
                totalAmount = total,
                percentage = percentage.toFloat(),
                count = list.size
            )
        }
        .sortedByDescending { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Raporlar & Analiz",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Özet Kartları
            item {
                ReportSummaryCard(
                    income = totalIncome,
                    expense = totalExpense,
                    netBalance = netBalance
                )
            }

            // 2. Gelir / Gider Oran Çubuğu
            item {
                IncomeExpenseRatioCard(income = totalIncome, expense = totalExpense)
            }

            // 3. Kategori Dağılımı Filtresi
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kategori Analizi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == "GIDER",
                            onClick = { selectedFilter = "GIDER" },
                            label = { Text("Giderler") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "GELIR",
                            onClick = { selectedFilter = "GELIR" },
                            label = { Text("Gelirler") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 4. Kategori Listesi
            if (categorySummaryList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Henüz bu türde kayıtlı işlem bulunmuyor.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(categorySummaryList) { summary ->
                    CategoryProgressItem(
                        summary = summary,
                        isExpense = selectedFilter == "GIDER"
                    )
                }
            }

            // 5. Ek İstatistikler Kartı
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StatisticsOverviewCard(transactions = transactions)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

data class CategorySummary(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

@Composable
fun ReportSummaryCard(income: Double, expense: Double, netBalance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Net Finansal Durum",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "₺${String.format(Locale.getDefault(), "%.2f", netBalance)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↑", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Top. Gelir", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "₺${String.format(Locale.getDefault(), "%.2f", income)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF44336).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↓", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Top. Gider", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "₺${String.format(Locale.getDefault(), "%.2f", expense)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseRatioCard(income: Double, expense: Double) {
    val total = income + expense
    val incomeProgress = if (total > 0) (income / total).toFloat() else 0.5f
    val animatedProgress by animateFloatAsState(targetValue = incomeProgress, label = "progress")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gelir / Gider Dağılımı",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "%${(incomeProgress * 100).toInt()} Gelir - %${((1 - incomeProgress) * 100).toInt()} Gider",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ClipProgressRatioBar(progress = animatedProgress)
        }
    }
}

@Composable
fun ClipProgressRatioBar(progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF44336)) // Base Gider rengi (Kırmızı)
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .background(Color(0xFF4CAF50)) // Gelir rengi (Yeşil)
            )
        }
    }
}

@Composable
fun CategoryProgressItem(summary: CategorySummary, isExpense: Boolean) {
    val barColor = if (isExpense) Color(0xFFF44336) else Color(0xFF4CAF50)
    val categoryIcon = getCategoryIcon(summary.categoryName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(barColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = summary.categoryName,
                            tint = barColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = summary.categoryName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${summary.count} işlem)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "₺${String.format(Locale.getDefault(), "%.2f", summary.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { summary.percentage.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = barColor.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "%${(summary.percentage * 100).toInt()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatisticsOverviewCard(transactions: List<Transaction>) {
    val totalCount = transactions.size
    val maxExpense = transactions
        .filter { it.type == "GIDER" || it.type == "EXPENSE" }
        .maxOfOrNull { it.amount } ?: 0.0
    val avgTransaction = if (totalCount > 0) transactions.sumOf { it.amount } / totalCount else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Genel İstatistikler",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(title = "İşlem Sayısı", value = "$totalCount ad.")
                StatItem(
                    title = "En Yüksek Gider",
                    value = "₺${String.format(Locale.getDefault(), "%.2f", maxExpense)}"
                )
                StatItem(
                    title = "Ort. İşlem",
                    value = "₺${String.format(Locale.getDefault(), "%.2f", avgTransaction)}"
                )
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}