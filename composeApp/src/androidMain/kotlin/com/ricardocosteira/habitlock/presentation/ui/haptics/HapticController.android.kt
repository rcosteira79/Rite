package com.ricardocosteira.habitlock.presentation.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

actual class HapticController(private val context: Context) {

    actual fun tick() {
        vibrate(FeedbackType.TICK)
    }

    actual fun click() {
        vibrate(FeedbackType.CLICK)
    }

    actual fun heavyClick() {
        vibrate(FeedbackType.HEAVY_CLICK)
    }

    private fun vibrate(type: FeedbackType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        }

        if (vibrator == null || !vibrator.hasVibrator()) return

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> tryCompositionHaptic(vibrator, type)
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> tryWaveformHaptic(vibrator, type)
            else -> triggerLegacyHaptic(vibrator, type)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun tryCompositionHaptic(vibrator: Vibrator, type: FeedbackType) {
        try {
            vibrator.vibrate(createComposition(type, vibrator))
        } catch (_: Exception) {
            tryWaveformHaptic(vibrator, type)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createComposition(
        type: FeedbackType,
        vibrator: Vibrator
    ): VibrationEffect {
        val (primitive, scale) = when (type) {
            FeedbackType.TICK -> VibrationEffect.Composition.PRIMITIVE_TICK to 0.4f
            FeedbackType.CLICK -> VibrationEffect.Composition.PRIMITIVE_CLICK to 0.6f
            FeedbackType.HEAVY_CLICK -> VibrationEffect.Composition.PRIMITIVE_THUD to 1.0f
        }

        if (!vibrator.areAllPrimitivesSupported(primitive)) {
            throw Exception("Device does not support required haptic primitive: $primitive")
        }

        return VibrationEffect.startComposition()
            .addPrimitive(primitive, scale)
            .compose()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun tryWaveformHaptic(vibrator: Vibrator, type: FeedbackType) {
        try {
            if (vibrator.hasAmplitudeControl()) {
                vibrator.vibrate(createWaveformEffect(type))
            } else {
                triggerLegacyHaptic(vibrator, type)
            }
        } catch (_: Exception) {
            triggerLegacyHaptic(vibrator, type)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createWaveformEffect(type: FeedbackType): VibrationEffect {
        val effect = when (type) {
            FeedbackType.TICK -> VibrationEffect.EFFECT_TICK
            FeedbackType.CLICK -> VibrationEffect.EFFECT_CLICK
            FeedbackType.HEAVY_CLICK -> VibrationEffect.EFFECT_HEAVY_CLICK
        }

        return VibrationEffect.createPredefined(effect)
    }

    private fun triggerLegacyHaptic(vibrator: Vibrator, type: FeedbackType) {
        val (duration, amplitude) = when (type) {
            FeedbackType.TICK -> 10L to 5L
            FeedbackType.CLICK -> 10L to 10L
            FeedbackType.HEAVY_CLICK -> 10L to 20L
        }

        @Suppress("DEPRECATION")
        vibrator.vibrate(longArrayOf(duration, amplitude), -1)
    }
}
