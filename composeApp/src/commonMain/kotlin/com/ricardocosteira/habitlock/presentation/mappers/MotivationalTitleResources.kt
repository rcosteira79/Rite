package com.ricardocosteira.habitlock.presentation.mappers

import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.motivational_title_0
import habitlock.composeapp.generated.resources.motivational_title_1
import habitlock.composeapp.generated.resources.motivational_title_10
import habitlock.composeapp.generated.resources.motivational_title_11
import habitlock.composeapp.generated.resources.motivational_title_12
import habitlock.composeapp.generated.resources.motivational_title_13
import habitlock.composeapp.generated.resources.motivational_title_14
import habitlock.composeapp.generated.resources.motivational_title_2
import habitlock.composeapp.generated.resources.motivational_title_3
import habitlock.composeapp.generated.resources.motivational_title_4
import habitlock.composeapp.generated.resources.motivational_title_5
import habitlock.composeapp.generated.resources.motivational_title_6
import habitlock.composeapp.generated.resources.motivational_title_7
import habitlock.composeapp.generated.resources.motivational_title_8
import habitlock.composeapp.generated.resources.motivational_title_9
import org.jetbrains.compose.resources.StringResource

private val MOTIVATIONAL_TITLE_RESOURCES: List<StringResource> = listOf(
    Res.string.motivational_title_0,
    Res.string.motivational_title_1,
    Res.string.motivational_title_2,
    Res.string.motivational_title_3,
    Res.string.motivational_title_4,
    Res.string.motivational_title_5,
    Res.string.motivational_title_6,
    Res.string.motivational_title_7,
    Res.string.motivational_title_8,
    Res.string.motivational_title_9,
    Res.string.motivational_title_10,
    Res.string.motivational_title_11,
    Res.string.motivational_title_12,
    Res.string.motivational_title_13,
    Res.string.motivational_title_14
)

fun motivationalTitleResource(index: Int): StringResource = MOTIVATIONAL_TITLE_RESOURCES[index]
