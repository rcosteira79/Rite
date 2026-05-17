package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

/**
 * Combined step header: `[STEP NAME mono] ━━━━━━━ [N / total mono]`.
 * Replaces the separate top "strap" + bottom indicator with a single row so
 * progress doesn't shift around the bottom CTA when buttons swap.
 */
@Composable
fun OnboardingStepStrap(
    step: Int,
    totalSteps: Int,
    stepName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stepName.uppercase(),
            style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp),
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(totalSteps) { index ->
                val isFilled = index < step
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (isFilled) {
                                RiteAppTheme.colors.onSurface
                            } else {
                                RiteAppTheme.colors.outline
                            }
                        )
                )
            }
        }

        Text(
            text = "$step / $totalSteps",
            style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
            color = RiteAppTheme.colors.onSurfaceVariant
        )
    }
}
