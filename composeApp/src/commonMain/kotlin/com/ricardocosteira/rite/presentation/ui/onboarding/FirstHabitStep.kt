package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.SchedulePicker
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_placeholder_habit_name
import rite.composeapp.generated.resources.common_quantitative
import rite.composeapp.generated.resources.first_habit_heading
import rite.composeapp.generated.resources.first_habit_label_name
import rite.composeapp.generated.resources.first_habit_label_target_value
import rite.composeapp.generated.resources.first_habit_label_unit
import rite.composeapp.generated.resources.first_habit_placeholder_unit
import rite.composeapp.generated.resources.first_habit_subtext
import rite.composeapp.generated.resources.first_habit_type_binary
import rite.composeapp.generated.resources.first_habit_type_binary_description
import rite.composeapp.generated.resources.first_habit_type_quantitative_description
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource

private val OnboardingTextFieldShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

private val HabitTypeCardIconSize = 26.dp

@Composable
private fun onboardingTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = RiteAppTheme.colorScheme.surfaceContainerHighest,
    unfocusedContainerColor = RiteAppTheme.colorScheme.surfaceContainerHighest,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@Composable
fun FirstHabitStep(
    habitName: String,
    habitType: HabitType,
    targetValue: String,
    unit: String,
    selectedDays: Set<DayOfWeek>,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(Res.string.first_habit_heading),
            style = RiteAppTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = RiteAppTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .background(
                    color = RiteAppTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.first_habit_subtext),
            style = RiteAppTheme.typography.bodyLarge,
            color = RiteAppTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = habitName,
            onValueChange = onHabitNameChange,
            label = { Text(stringResource(Res.string.first_habit_label_name)) },
            placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
            shape = OnboardingTextFieldShape,
            colors = onboardingTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        HabitTypeCard(
            icon = Icons.Outlined.CheckCircle,
            label = stringResource(Res.string.first_habit_type_binary),
            description = stringResource(Res.string.first_habit_type_binary_description),
            isSelected = habitType == HabitType.BINARY,
            onClick = { onHabitTypeChange(HabitType.BINARY) },
            expandedContent = null
        )

        Spacer(modifier = Modifier.height(8.dp))

        HabitTypeCard(
            icon = Icons.Outlined.ShowChart,
            label = stringResource(Res.string.common_quantitative),
            description = stringResource(Res.string.first_habit_type_quantitative_description),
            isSelected = habitType == HabitType.QUANTITATIVE,
            onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
            expandedContent = {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    TextField(
                        value = targetValue,
                        onValueChange = onTargetValueChange,
                        label = { Text(stringResource(Res.string.first_habit_label_target_value)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = OnboardingTextFieldShape,
                        colors = onboardingTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text(stringResource(Res.string.first_habit_label_unit)) },
                        placeholder = {
                            Text(stringResource(Res.string.first_habit_placeholder_unit))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = OnboardingTextFieldShape,
                        colors = onboardingTextFieldColors()
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SchedulePicker(
            selectedDays = selectedDays,
            onSelectedDaysChange = onSelectedDaysChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HabitTypeCard(
    icon: ImageVector,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    expandedContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, RiteAppTheme.colorScheme.primaryContainer)
        } else {
            BorderStroke(2.dp, Color.Transparent)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                RiteAppTheme.colorScheme.surfaceContainerHighest
            } else {
                RiteAppTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(HabitTypeCardIconSize),
                tint = if (isSelected) {
                    RiteAppTheme.colorScheme.primary
                } else {
                    RiteAppTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = RiteAppTheme.typography.titleSmall,
                color = RiteAppTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = RiteAppTheme.typography.bodySmall,
                color = RiteAppTheme.colorScheme.onSurfaceVariant
            )
            if (expandedContent != null) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    expandedContent()
                }
            }
        }
    }
}
