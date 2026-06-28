package com.ricardocosteira.rite.presentation.ui.onboarding

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.strictness_balanced_description
import rite.composeapp.generated.resources.strictness_balanced_label
import rite.composeapp.generated.resources.strictness_balanced_rule_1
import rite.composeapp.generated.resources.strictness_balanced_rule_2
import rite.composeapp.generated.resources.strictness_balanced_rule_3
import rite.composeapp.generated.resources.strictness_balanced_rule_4
import rite.composeapp.generated.resources.strictness_flexible_description
import rite.composeapp.generated.resources.strictness_flexible_label
import rite.composeapp.generated.resources.strictness_flexible_rule_1
import rite.composeapp.generated.resources.strictness_flexible_rule_2
import rite.composeapp.generated.resources.strictness_flexible_rule_3
import rite.composeapp.generated.resources.strictness_flexible_rule_4
import rite.composeapp.generated.resources.strictness_locked_description
import rite.composeapp.generated.resources.strictness_locked_label
import rite.composeapp.generated.resources.strictness_locked_rule_1
import rite.composeapp.generated.resources.strictness_locked_rule_2
import rite.composeapp.generated.resources.strictness_locked_rule_3
import rite.composeapp.generated.resources.strictness_locked_rule_4

enum class OnboardingStrictnessPreset(
    val labelRes: StringResource,
    val descriptionRes: StringResource,
    val ruleResources: ImmutableList<StringResource>,
    val isRecommended: Boolean = false
) {
    FLEXIBLE(
        labelRes = Res.string.strictness_flexible_label,
        descriptionRes = Res.string.strictness_flexible_description,
        ruleResources = persistentListOf(
            Res.string.strictness_flexible_rule_1,
            Res.string.strictness_flexible_rule_2,
            Res.string.strictness_flexible_rule_3,
            Res.string.strictness_flexible_rule_4
        )
    ),
    BALANCED(
        labelRes = Res.string.strictness_balanced_label,
        descriptionRes = Res.string.strictness_balanced_description,
        ruleResources = persistentListOf(
            Res.string.strictness_balanced_rule_1,
            Res.string.strictness_balanced_rule_2,
            Res.string.strictness_balanced_rule_3,
            Res.string.strictness_balanced_rule_4
        ),
        isRecommended = true
    ),
    UNWAVERING(
        labelRes = Res.string.strictness_locked_label,
        descriptionRes = Res.string.strictness_locked_description,
        ruleResources = persistentListOf(
            Res.string.strictness_locked_rule_1,
            Res.string.strictness_locked_rule_2,
            Res.string.strictness_locked_rule_3,
            Res.string.strictness_locked_rule_4
        )
    )
}
