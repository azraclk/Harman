package app.azracelik.harman.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.azracelik.harman.R
import app.azracelik.harman.data.model.SavingsGoal
import app.azracelik.harman.domain.formatMoney
import app.azracelik.harman.domain.minorToInput
import app.azracelik.harman.domain.parseMoneyToMinor
import app.azracelik.harman.ui.GoalsViewModel
import app.azracelik.harman.ui.ViewModelFactory
import app.azracelik.harman.ui.components.BentoTile
import app.azracelik.harman.ui.components.SoftProgressBar
import app.azracelik.harman.ui.theme.Tile
import app.azracelik.harman.ui.theme.semantic

@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = viewModel(factory = ViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) } // null & showCreateDialog = yeni hedef
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.goals_title), style = MaterialTheme.typography.headlineLarge)
        LimitCard(state.limitMinor, onSave = viewModel::setLimit)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.goals_savings_title), style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = {
                    editingGoal = null
                    showCreateDialog = true
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.goals_goal_name))
            }
        }

        if (state.goals.isEmpty()) {
            BentoTile(MaterialTheme.semantic.butter, Modifier.fillMaxWidth(), contentPadding = 20.dp) {
                Text(
                    stringResource(R.string.goal_card_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            state.goals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    onEdit = { editingGoal = goal },
                    onAdd = { delta -> viewModel.addToGoal(goal.id, delta) },
                    onDelete = { viewModel.deleteGoal(goal) }
                )
            }
        }
    }

    if (showCreateDialog || editingGoal != null) {
        GoalEditDialog(
            initialGoal = editingGoal,
            onDismiss = {
                showCreateDialog = false
                editingGoal = null
            },
            onSave = { id, name, targetMinor ->
                viewModel.saveGoal(id, name, targetMinor)
            }
        )
    }
}

@Composable
private fun SectionCard(title: String, tile: Tile, content: @Composable () -> Unit) {
    BentoTile(tile, Modifier.fillMaxWidth(), contentPadding = 20.dp) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun LimitCard(limitMinor: Long, onSave: (Long) -> Unit) {
    var text by remember(limitMinor) { mutableStateOf(minorToInput(limitMinor)) }
    val parsed = parseMoneyToMinor(text)
    val changed = if (text.isBlank()) limitMinor != 0L else parsed != null && parsed != limitMinor

    SectionCard(stringResource(R.string.goals_limit_title), MaterialTheme.semantic.lilac) {
        MoneyField(text, { text = it }, stringResource(R.string.goals_limit_title))
        Text(
            stringResource(R.string.goals_limit_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = {
                val value = if (text.isBlank()) 0L else parsed
                if (value != null) onSave(value)
            },
            enabled = changed,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) { Text(stringResource(R.string.save)) }
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    onEdit: () -> Unit,
    onAdd: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var adjustSign by remember { mutableStateOf<Int?>(null) } // +1 ekle, -1 çıkar

    BentoTile(MaterialTheme.semantic.butter, Modifier.fillMaxWidth(), contentPadding = 20.dp) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(goal.name, style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
        }
        SoftProgressBar(
            fraction = if (goal.targetMinor > 0) goal.savedMinor.toFloat() / goal.targetMinor else 0f,
            color = MaterialTheme.colorScheme.primary,
            height = 14.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            stringResource(
                R.string.goals_goal_saved,
                formatMoney(goal.savedMinor, false),
                formatMoney(goal.targetMinor, false)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (goal.savedMinor >= goal.targetMinor) {
            Text(
                stringResource(R.string.goals_reached),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.semantic.income,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
            Button(
                onClick = { adjustSign = 1 },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) { Text(stringResource(R.string.goals_add_saving)) }
            OutlinedButton(
                onClick = { adjustSign = -1 },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) { Text(stringResource(R.string.goals_remove_saving)) }
        }
        TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(stringResource(R.string.goals_delete_goal), color = MaterialTheme.semantic.expense)
        }
    }

    adjustSign?.let { sign ->
        var amountText by remember { mutableStateOf("") }
        val minor = parseMoneyToMinor(amountText)
        AlertDialog(
            onDismissRequest = { adjustSign = null },
            title = {
                Text(stringResource(if (sign > 0) R.string.goals_add_saving else R.string.goals_remove_saving))
            },
            text = { MoneyField(amountText, { amountText = it }, stringResource(R.string.goals_amount)) },
            confirmButton = {
                TextButton(
                    enabled = minor != null,
                    onClick = {
                        onAdd(sign * (minor ?: 0L))
                        adjustSign = null
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { adjustSign = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun GoalEditDialog(
    initialGoal: SavingsGoal?,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, targetMinor: Long) -> Unit
) {
    var name by remember { mutableStateOf(initialGoal?.name ?: "") }
    var targetText by remember { mutableStateOf(initialGoal?.let { minorToInput(it.targetMinor) } ?: "") }
    val targetMinor = parseMoneyToMinor(targetText)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) stringResource(R.string.goals_savings_title) else stringResource(R.string.edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text(stringResource(R.string.goals_goal_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors()
                )
                MoneyField(targetText, { targetText = it }, stringResource(R.string.goals_goal_target))
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && targetMinor != null,
                onClick = {
                    if (targetMinor != null) {
                        onSave(initialGoal?.id ?: 0L, name.trim(), targetMinor)
                        onDismiss()
                    }
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
internal fun MoneyField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() || it == ',' || it == '.' }.take(14)) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        suffix = { Text("₺") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors()
    )
}

@Composable
internal fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent
)
