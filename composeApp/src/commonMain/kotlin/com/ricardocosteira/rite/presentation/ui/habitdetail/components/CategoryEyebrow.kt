package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_category_binary
import rite.composeapp.generated.resources.habit_detail_category_quantitative

@Composable
fun CategoryEyebrow(type: HabitType, modifier: Modifier = Modifier,) {
    val text: String = when (type) {
        HabitType.BINARY -> stringResource(Res.string.habit_detail_category_binary)
        HabitType.QUANTITATIVE -> stringResource(Res.string.habit_detail_category_quantitative)
    }
    Text(
        text = text,
        style = RiteAppTheme.typography.eyebrow,
        color = RiteAppTheme.colors.onSurfaceMuted,
        modifier = modifier,
    )
}
