package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.inter
import habitlock.composeapp.generated.resources.manrope
import org.jetbrains.compose.resources.Font

@Composable
private fun manropeFontFamily(): FontFamily = FontFamily(
    Font(Res.font.manrope, FontWeight.Normal),
    Font(Res.font.manrope, FontWeight.Medium),
    Font(Res.font.manrope, FontWeight.SemiBold),
    Font(Res.font.manrope, FontWeight.Bold),
    Font(Res.font.manrope, FontWeight.ExtraBold)
)

@Composable
private fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter, FontWeight.Normal),
    Font(Res.font.inter, FontWeight.Medium),
    Font(Res.font.inter, FontWeight.SemiBold)
)

@Composable
internal fun habitLockTypography(): Typography {
    val manrope = manropeFontFamily()
    val inter = interFontFamily()

    return Typography(
        // Display — Manrope, for emotional milestones (3.5rem ≈ 56sp)
        displayLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 56.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium =
            TextStyle(
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp
            ),
        displaySmall =
            TextStyle(
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp
            ),
        // Headlines — Manrope, screen titles (1.75rem = 28sp)
        headlineLarge =
            TextStyle(
                fontFamily = manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp
            ),
        headlineMedium =
            TextStyle(
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp
            ),
        headlineSmall =
            TextStyle(
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp
            ),
        // Titles — Inter (1.375rem = 22sp for titleLarge)
        titleLarge =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
        titleMedium =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            ),
        titleSmall =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
        // Body — Inter (0.875rem = 14sp for bodyMedium)
        bodyLarge =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
        bodyMedium =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            ),
        bodySmall =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            ),
        // Labels — Inter
        labelLarge =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
        labelMedium =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
        labelSmall =
            TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
    )
}
