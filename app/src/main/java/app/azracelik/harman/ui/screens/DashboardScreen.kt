package app.azracelik.harman.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.harman.data.DateFilterHelper
import app.azracelik.harman.data.DateFilterPeriod
import app.azracelik.harman.data.Transaction
import app.azracelik.harman.ui.theme.ExpenseContainer
import app.azracelik.harman.ui.theme.ExpenseTerracotta
import app.azracelik.harman.ui.theme.IncomeContainer
import app.azracelik.harman.ui.theme.IncomeGreen
import app.azracelik.harman.ui.theme.getCategoryIcon
import app.azracelik.harman.viewmodel.TransactionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel
) {
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    var selectedPeriod by remember { mutableStateOf(DateFilterPeriod.THIS_MONTH) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // 1. Tarih Aralığı Hesaplama
    val (startMs, endMs) = remember(selectedPeriod) {
        DateFilterHelper.getDateRange(selectedPeriod)
    }

    val periodDateText = remember(startMs, endMs, selectedPeriod) {
        DateFilterHelper.formatDateRange(startMs, endMs, selectedPeriod)
    }

    // 2. İşlemleri Tarihe ve Arama Sorgusuna Göre Filtreleme
    val filteredTransactions = remember(allTransactions, startMs, endMs, searchQuery, selectedPeriod) {
        allTransactions.filter { transaction ->
            val matchesDate = if (selectedPeriod == DateFilterPeriod.ALL_TIME) {
                true
            } else {
                transaction.date in startMs..endMs
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                transaction.categoryName.contains(searchQuery, ignoreCase = true) ||
                        transaction.note.contains(searchQuery, ignoreCase = true)
            }

            matchesDate && matchesSearch
        }
    }

    val totalIncome = filteredTransactions
        .filter { it.type == "GELIR" || it.type == "INCOME" }
        .sumOf { it.amount }
    val totalExpense = filteredTransactions
        .filter { it.type == "GIDER" || it.type == "EXPENSE" }
        .sumOf { it.amount }
    val remainingBalance = totalIncome - totalExpense

    // Tümünü Sil Onay Diyalogu
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Tüm İşlemleri Sil") },
            text = { Text("Kaydedilmiş tüm gelir ve gider verileri temizlenecektir. Emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllTransactions()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Evet, Temizle", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    // Tekli İşlem Silme Onay Diyalogu
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("İşlemi Sil") },
            text = { Text("${transactionToDelete?.categoryName} işlemini silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TopHeaderBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Üst Bar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "harman",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                },
                actions = {
                    if (allTransactions.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Tümünü Temizle",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Kalan Bütçe Halka Göstergesi
                item {
                    CircularBudgetGauge(
                        remainingBalance = remainingBalance,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        periodDateText = periodDateText
                    )
                }

                // 2. Dönem Seçici Filtre Çipleri
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DateFilterPeriod.entries.toTypedArray()) { period ->
                            val isSelected = selectedPeriod == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPeriod = period },
                                label = { Text(period.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // 3. Arama Çubuğu (Search Bar)
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Kategori veya not ara...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Ara",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Temizle",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }

                // 4. Son İşlemler Başlığı
                item {
                    Text(
                        text = "Son İşlemler",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // 5. İşlem Listesi veya Boş Durum
                if (filteredTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Aramanızla eşleşen işlem bulunamadı." else "Bu dönemde kaydedilmiş işlem bulunmuyor.",
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTransactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDeleteClick = { transactionToDelete = transaction }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeaderBackground() {
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    val secondaryColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width

        drawCircle(
            color = secondaryColor,
            radius = width * 0.55f,
            center = Offset(width * 0.5f, -width * 0.12f)
        )

        drawCircle(
            color = primaryColor,
            radius = width * 0.42f,
            center = Offset(width * 0.65f, -width * 0.05f)
        )
    }
}

@Composable
fun CircularBudgetGauge(
    remainingBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    periodDateText: String,
    modifier: Modifier = Modifier
) {
    val progressRatio = if (totalIncome > 0) {
        (remainingBalance / totalIncome).coerceIn(0.0, 1.0).toFloat()
    } else if (remainingBalance > 0) 1.0f else 0.0f

    val animatedProgress by animateFloatAsState(targetValue = progressRatio, label = "gauge")

    val trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
    val strokeColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val strokeWidthPx = 16.dp.toPx()

                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = strokeColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "kalan bütçe",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = periodDateText,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₺${String.format(Locale.getDefault(), "%.2f", remainingBalance)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IncomeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↑", color = IncomeGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Gelir", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "₺${String.format(Locale.getDefault(), "%.2f", totalIncome)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ExpenseContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↓", color = ExpenseTerracotta, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Gider", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "₺${String.format(Locale.getDefault(), "%.2f", totalExpense)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseTerracotta
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDeleteClick: () -> Unit
) {
    val isIncome = transaction.type == "GELIR" || transaction.type == "INCOME"
    val amountColor = if (isIncome) IncomeGreen else ExpenseTerracotta
    val amountPrefix = if (isIncome) "+" else "-"
    val categoryIcon = getCategoryIcon(transaction.categoryName)
    val formattedDate = remember(transaction.date) {
        DateFilterHelper.formatTransactionDate(transaction.date)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = transaction.categoryName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.categoryName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (transaction.note.isNotBlank()) {
                        Text(
                            text = transaction.note,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amountPrefix ₺${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sil",
                        tint = Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}