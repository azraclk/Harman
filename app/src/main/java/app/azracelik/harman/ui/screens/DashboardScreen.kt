package app.azracelik.harman.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
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
import app.azracelik.harman.data.Transaction
import app.azracelik.harman.ui.theme.getCategoryIcon
import app.azracelik.harman.viewmodel.TransactionViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }

    val totalIncome = transactions.filter { it.type == "GELIR" || it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "GIDER" || it.type == "EXPENSE" }.sumOf { it.amount }
    val remainingBalance = totalIncome - totalExpense

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Tüm İşlemleri Sil") },
            text = { Text("Kaydedilmiş tüm gelir ve gider verileri temizlenecektir. Emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllTransactions()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Evet, Temizle", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Üst Arka Plan Dalga/Daire Desenleri
        TopHeaderBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
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
                    if (transactions.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
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

            // Duyarlı ve Kaydırılabilir İçerik
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Kalan Bütçe Halka Göstergesi
                item {
                    CircularBudgetGauge(
                        remainingBalance = remainingBalance,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense
                    )
                }

                // Son İşlemler Başlığı
                item {
                    Text(
                        text = "Son İşlemler",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // İşlem Listesi veya Boş Durum
                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Henüz işlem bulunmuyor.\nAlt bardaki '+' butonuna basarak ekleyebilirsiniz.",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        TransactionItem(transaction = transaction)
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
            // Dairesel Halka Göstergesi
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

                    // Arka Plan İlerleme Halkası
                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // Ön Plan Aktif Halka
                    drawArc(
                        color = strokeColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }

                // Halka İçi Metin
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "kalan bütçe",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = getCurrentMonthPeriod(),
                        fontSize = 11.sp,
                        color = Color.Gray
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

            // Gelir / Gider Alt Detayları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↑", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Gelir", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "₺${String.format(Locale.getDefault(), "%.2f", totalIncome)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
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
                            .background(Color(0xFFF44336).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↓", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Gider", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "₺${String.format(Locale.getDefault(), "%.2f", totalExpense)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == "GELIR" || transaction.type == "INCOME"
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val amountPrefix = if (isIncome) "+" else "-"
    val categoryIcon = getCategoryIcon(transaction.categoryName)

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Kategori İkon Rozeti
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
                }
            }

            Text(
                text = "$amountPrefix ₺${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

fun getCurrentMonthPeriod(): String {
    val calendar = Calendar.getInstance()
    val monthNames = arrayOf("Eyl", "Ekm", "Kas", "Ara", "Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu")
    val monthIndex = calendar.get(Calendar.MONTH)
    val monthName = monthNames[monthIndex % monthNames.size]
    return "1 $monthName - 30 $monthName"
}