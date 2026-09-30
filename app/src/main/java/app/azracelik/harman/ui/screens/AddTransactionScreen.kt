package app.azracelik.harman.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.harman.data.Transaction
import app.azracelik.harman.ui.theme.getCategoryIcon
import app.azracelik.harman.viewmodel.TransactionViewModel

val predefinedCategories = listOf(
    "Maaş",
    "Faturalar & Abonelikler",
    "Sosyal & Eğlence",
    "Market",
    "Ulaşım",
    "Alışveriş",
    "Sağlık & Bakım",
    "Eğitim & Kariyer",
    "Yatırım",
    "Diğer"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    var isIncome by remember { mutableStateOf(false) } // false = Gider, true = Gelir
    var categoryName by remember { mutableStateOf(predefinedCategories.first()) }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni İşlem Ekle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Gelir / Gider Seçim Butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isIncome = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isIncome) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isIncome) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gider", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isIncome = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isIncome) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gelir", fontWeight = FontWeight.Bold)
                }
            }

            // 2. Kategori Seçimi Kartı & Dropdown (İkonlu)
            Text(
                text = "Kategori Seçin",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getCategoryIcon(categoryName),
                                contentDescription = categoryName,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Kategori",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = categoryName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Kategori Seç"
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    predefinedCategories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = category,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(category)
                                }
                            },
                            onClick = {
                                categoryName = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 3. Not / Açıklama Girişi
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Açıklama / Not (İsteğe Bağlı)") },
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Tutar Girişi
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Tutar (₺)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kaydet Butonu
            Button(
                onClick = {
                    val sanitizedAmount = amount.replace(',', '.')
                    val amountValue = sanitizedAmount.toDoubleOrNull() ?: 0.0
                    if (categoryName.isNotBlank() && amountValue > 0) {
                        val newTransaction = Transaction(
                            amount = amountValue,
                            type = if (isIncome) "GELIR" else "GIDER",
                            categoryName = categoryName,
                            date = System.currentTimeMillis(),
                            note = note
                        )
                        viewModel.addTransaction(newTransaction)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = categoryName.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}