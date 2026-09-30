package app.azracelik.harman.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.harman.data.Transaction
import app.azracelik.harman.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    onAddClick: () -> Unit // Yeni işlem ekleme ekranına geçiş için tetikleyici
) {
    // ViewModel'daki Flow'u Compose'un anlayacağı 'State' yapısına çeviriyoruz
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    // Basit bakiye hesaplamaları
    val totalIncome = transactions.filter { it.type == "GELIR" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "GIDER" }.sumOf { it.amount }
    val currentBalance = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "İşlem Ekle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. Üst Kısım: Bakiye Kartı
            BalanceCard(balance = currentBalance, income = totalIncome, expense = totalExpense)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Son İşlemler",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Alt Kısım: İşlem Listesi
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Toplam Bakiye", fontSize = 16.sp, color = Color.Gray)
            Text(
                text = "₺${String.format("%.2f", balance)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gelir", color = Color.Gray)
                    Text("₺${String.format("%.2f", income)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) // Yeşil
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gider", color = Color.Gray)
                    Text("₺${String.format("%.2f", expense)}", color = Color(0xFFF44336), fontWeight = FontWeight.Bold) // Kırmızı
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == "GELIR"
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val amountPrefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = transaction.categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = transaction.note, fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "$amountPrefix ₺${String.format("%.2f", transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}