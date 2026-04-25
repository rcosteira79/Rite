package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.fraunces
import rite.composeapp.generated.resources.fraunces_italic
import rite.composeapp.generated.resources.inter_tight
import rite.composeapp.generated.resources.jetbrains_mono

@Immutable
data class RiteTypography(
    val displayLarge: TextStyle = TextStyle.Default,
    val displayMedium: TextStyle = TextStyle.Default,
    val displaySmall: TextStyle = TextStyle.Default,
    val headlineLarge: TextStyle = TextStyle.Default,
    val headlineMedium: TextStyle = TextStyle.Default,
    val headlineSmall: TextStyle = TextStyle.Default,
    val titleLarge: TextStyle = TextStyle.Default,
    val titleMedium: TextStyle = TextStyle.Default,
    val titleSmall: TextStyle = TextStyle.Default,
    val bodyLarge: TextStyle = TextStyle.Default,
    val bodyMedium: TextStyle = TextStyle.Default,
    val bodySmall: TextStyle = TextStyle.Default,
    val labelLarge: TextStyle = TextStyle.Default,
    val labelMedium: TextStyle = TextStyle.Default,
    val labelSmall: TextStyle = TextStyle.Default,
    // Rite extensions
    val eyebrow: TextStyle = TextStyle.Default,
    val displayItalic: TextStyle = TextStyle.Default,
    val mono: TextStyle = TextStyle.Default
) {
    fun toMaterialTypography(): Typography = Typography(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = displaySmall,
        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall,
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,
        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall
    )
}

val LocalRiteTypography = staticCompositionLocalOf { RiteTypography() }

@Composable
fun riteTypography(): RiteTypography {
    val fraunces = FontFamily(
        Font(Res.font.fraunces, FontWeight.Light),
        Font(Res.font.fraunces, FontWeight.Normal),
        Font(Res.font.fraunces, FontWeight.Medium),
        Font(Res.font.fraunces, FontWeight.SemiBold),
        Font(Res.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.fraunces_italic, FontWeight.Medium, FontStyle.Italic)
    )
    val interTight = FontFamily(
        Font(Res.font.inter_tight, FontWeight.Normal),
        Font(Res.font.inter_tight, FontWeight.Medium),
        Font(Res.font.inter_tight, FontWeight.SemiBold),
        Font(Res.font.inter_tight, FontWeight.Bold)
    )
    val jetbrainsMono = FontFamily(
        Font(Res.font.jetbrains_mono, FontWeight.Normal),
        Font(Res.font.jetbrains_mono, FontWeight.Medium)
    )

    return RiteTypography(
        displayLarge = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Light,
            fontSize = 64.sp,
            lineHeight = 72.sp,
            letterSpacing = (-0.02).em
        ),
        displayMedium = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Normal,
            fontSize = 44.sp,
            lineHeight = 52.sp,
            letterSpacing = (-0.01).em
        ),
        displaySmall = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.01).em
        ),
        headlineLarge = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.01).em
        ),
        headlineMedium = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.01).em
        ),
        headlineSmall = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.01).em
        ),
        titleMedium = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        titleSmall = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodySmall = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.2.sp
        ),
        labelLarge = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp
        ),
        labelSmall = TextStyle(
            fontFamily = interTight,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.2.sp
        ),
        // Rite extensions
        eyebrow = TextStyle(
            fontFamily = jetbrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.18.em
        ),
        displayItalic = TextStyle(
            fontFamily = fraunces,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.01).em
        ),
        mono = TextStyle(
            fontFamily = jetbrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.2.em
        )
    )
}
