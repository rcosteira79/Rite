package com.ricardocosteira.habitlock.presentation.ui

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringRes(val resource: StringResource) : UiText()
}

suspend fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringRes -> getString(resource)
}
