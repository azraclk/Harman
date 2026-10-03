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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.goals_title), style = MaterialTheme.typography.headlineLarge)
        LimitCard(state.limitMinor, onSave = viewModel::setLimit)
        SavingsCard(
            goal = state.goal,
            onSaveGoal = viewModel::saveGoal,
            onAdd = viewModel::addToGoal,
            onDelete = viewModel::deleteGoal
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
private fun SavingsCard(
    goal: SavingsGoal?,
    onSaveGoal: (String, Long) -> Unit,
    onAdd: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var editing by remember(goal?.id, goal == null) { mutableStateOf(goal == null) }
    var name by remember(goal) { mutableStateOf(goal?.name ?: "") }
    var target by remember(goal) { mutableStateOf(goal?.let { minorToInput(it.targetMinor) } ?: "") }
    var adjust by remember { mutableStateOf<Int?>(null) } // +1 ekle, -1 çıkar

    SectionCard(stringResource(R.string.goals_savings_title), MaterialTheme.semantic.butter) {
        if (editing || goal == null) {
            TextField(
                value = name,
                onValueChange = { name = it.take(30) },
                label = { Text(stringResource(R.string.goals_goal_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )
            MoneyField(target, { target = it }, stringResource(R.string.goals_goal_target))
            val targetMinor = parseMoneyToMinor(target)
            Button(
                onClick = {
                    if (targetMinor != null) {
                        onSaveGoal(name.trim(), targetMinor)
                        editing = false
                    }
                },
                enabled = name.isNotBlank() && targetMinor != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text(stringResource(R.string.save)) }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(goal.name, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { editing = true }) { Text(stringResource(R.string.edit)) }
            }
            SoftProgressBar(
                fraction = if (goal.targetMinor > 0) goal.savedMinor.toFloat() / goal.targetMinor else 0f,
                color = MaterialTheme.colorScheme.primary,
                height = 14.dp
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
                    color = MaterialTheme.semantic.income
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { adjust = 1 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) { Text(stringResource(R.string.goals_add_saving)) }
                OutlinedButton(
                    onClick = { adjust = -1 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) { Text(stringResource(R.string.goals_remove_saving)) }
            }
            TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.goals_delete_goal), color = MaterialTheme.semantic.expense)
            }
        }
    }

    adjust?.let { sign ->
        var amount by remember { mutableStateOf("") }
        val minor = parseMoneyToMinor(amount)
        AlertDialog(
            onDismissRequest = { adjust = null },
            title = {
                Text(stringResource(if (sign > 0) R.string.goals_add_saving else R.string.goals_remove_saving))
            },
            text = { MoneyField(amount, { amount = it }, stringResource(R.string.goals_amount)) },
            confirmButton = {
                TextButton(
                    enabled = minor != null,
                    onClick = {
                        onAdd(sign * (minor ?: 0L))
                        adjust = null
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { adjust = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
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
