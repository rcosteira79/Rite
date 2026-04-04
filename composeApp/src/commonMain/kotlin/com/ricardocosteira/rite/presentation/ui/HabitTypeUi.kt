package com.ricardocosteira.rite.presentation.ui

import com.ricardocosteira.rite.domain.models.HabitType
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_quantitative
import rite.composeapp.generated.resources.habit_form_type_binary_label
import org.jetbrains.compose.resources.StringResource

val HabitType.labelRes: StringResource
    get() = when (this) {
        HabitType.BINARY -> Res.string.habit_form_type_binary_label
        HabitType.QUANTITATIVE -> Res.string.common_quantitative
    }
