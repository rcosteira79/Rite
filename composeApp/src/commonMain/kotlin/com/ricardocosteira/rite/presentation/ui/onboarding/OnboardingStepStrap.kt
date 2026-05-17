package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun OnboardingStepStrap(
    step: Int,
    totalSteps: Int,
    stepName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "STEP $step OF $totalSteps · ${stepName.uppercase()}",
        style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp),
        color = RiteAppTheme.colors.onSurfaceVariant,
        modifier = modifier
    )
}
