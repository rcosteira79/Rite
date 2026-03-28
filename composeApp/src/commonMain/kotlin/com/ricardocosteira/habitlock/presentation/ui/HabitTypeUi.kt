package com.ricardocosteira.habitlock.presentation.ui

import com.ricardocosteira.habitlock.domain.models.HabitType
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_quantitative
import habitlock.composeapp.generated.resources.habit_form_type_binary_label
import org.jetbrains.compose.resources.StringResource

val HabitType.labelRes: StringResource
    get() =
        when (this) {
            HabitType.BINARY -> Res.string.habit_form_type_binary_label
            HabitType.QUANTITATIVE -> Res.string.common_quantitative
        }
