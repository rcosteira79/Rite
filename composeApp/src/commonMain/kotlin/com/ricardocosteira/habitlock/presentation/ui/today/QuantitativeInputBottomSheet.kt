package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_cancel
import habitlock.composeapp.generated.resources.quantitative_input_button_add
import habitlock.composeapp.generated.resources.quantitative_input_current_no_unit
import habitlock.composeapp.generated.resources.quantitative_input_current_with_unit
import habitlock.composeapp.generated.resources.quantitative_input_label_amount
import habitlock.composeapp.generated.resources.quantitative_input_quick_add
import habitlock.composeapp.generated.resources.quantitative_input_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantitativeInputBottomSheet(
    habit: TodayHabitUiModel,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var inputValue by remember { mutableStateOf("1") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.quantitative_input_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current progress
            if (habit.targetValue != null) {
                Text(
                    text = if (habit.unit != null) {
                        stringResource(
                            Res.string.quantitative_input_current_with_unit,
                            habit.completedValue ?: 0,
                            habit.targetValue,
                            habit.unit
                        )
                    } else {
                        stringResource(
                            Res.string.quantitative_input_current_no_unit,
                            habit.completedValue ?: 0,
                            habit.targetValue
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { newValue ->
                        // Only allow positive integers
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            inputValue = newValue
                        }
                    },
                    label = { Text(stringResource(Res.string.quantitative_input_label_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(120.dp)
                )

                if (habit.unit != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = habit.unit,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Quick add buttons
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 5, 10).forEach { amount ->
                    TextButton(
                        onClick = { inputValue = amount.toString() }
                    ) {
                        Text(stringResource(Res.string.quantitative_input_quick_add, amount))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.common_cancel))
                }

                Button(
                    onClick = {
                        val value = inputValue.toIntOrNull() ?: 1
                        if (value > 0) {
                            onConfirm(value)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = inputValue.toIntOrNull()?.let { it > 0 } == true
                ) {
                    Text(stringResource(Res.string.quantitative_input_button_add))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
