package app.azracelik.harman.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.azracelik.harman.R
import app.azracelik.harman.data.model.Category
import app.azracelik.harman.data.model.Transaction
import app.azracelik.harman.data.model.TransactionType
import app.azracelik.harman.domain.dayLabel
import app.azracelik.harman.domain.minorToInput
import app.azracelik.harman.domain.parseMoneyToMinor
import app.azracelik.harman.ui.components.icon
import app.azracelik.harman.ui.theme.semantic
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** [initial] null ise yeni işlem, değilse düzenleme. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionSheet(
    initial: Transaction?,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var type by remember { mutableStateOf(initial?.type ?: TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf(initial?.let { minorToInput(it.amountMinor) } ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: Category.MARKET) }
    var date by remember { mutableStateOf(initial?.let { LocalDate.ofEpochDay(it.epochDay) } ?: LocalDate.now()) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var showError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val amountMinor = parseMoneyToMinor(amountText)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(if (initial == null) R.string.tx_new else R.string.tx_edit),
                style = MaterialTheme.typography.headlineMedium
            )

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { index, t ->
                    SegmentedButton(
                        selected = type == t,
                        onClick = {
                            type = t
                            if (category.type != t) category = Category.forType(t).first()
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, TransactionType.entries.size)
                    ) {
                        Text(stringResource(if (t == TransactionType.EXPENSE) R.string.expense else R.string.income))
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { v -> amountText = v.filter { it.isDigit() || it == ',' || it == '.' }.take(14) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    color = if (type == TransactionType.INCOME) MaterialTheme.semantic.income
                    else MaterialTheme.colorScheme.onBackground
                ),
                label = { Text(stringResource(R.string.tx_amount)) },
                suffix = { Text("₺", style = MaterialTheme.typography.headlineMedium) },
                singleLine = true,
                isError = showError && amountMinor == null,
                supportingText = {
                    if (showError && amountMinor == null) Text(stringResource(R.string.tx_amount_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(20.dp)
            )

            Text(
                stringResource(R.string.tx_category),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.forType(type).forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = c },
                        label = { Text(stringResource(c.labelRes)) },
                        leadingIcon = { Icon(c.icon(), null, Modifier.size(18.dp)) },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Rounded.CalendarToday, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    if (date == LocalDate.now()) stringResource(R.string.tx_today) + " · " + date.dayLabel()
                    else date.dayLabel()
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(60) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.tx_note)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp)
            )

            Button(
                onClick = {
                    if (amountMinor == null) {
                        showError = true
                    } else {
                        onSave(
                            Transaction(
                                id = initial?.id ?: 0,
                                amountMinor = amountMinor,
                                type = type,
                                category = category,
                                epochDay = date.toEpochDay(),
                                note = note.trim()
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) { Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium) }

            if (initial != null) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.semantic.expense)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showDeleteConfirm && initial != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.tx_delete_title)) },
            text = { Text(stringResource(R.string.tx_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(initial)
                    showDeleteConfirm = false
                    onDismiss()
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.semantic.expense) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
