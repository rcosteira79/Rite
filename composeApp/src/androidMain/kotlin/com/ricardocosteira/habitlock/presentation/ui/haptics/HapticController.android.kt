package com.ricardocosteira.habitlock.presentation.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual class HapticController(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE
            ) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    actual fun tick() {
        vibrate(
            primitive = VibrationEffect.Composition.PRIMITIVE_TICK,
            primitiveScale = 0.4f,
            predefined = VibrationEffect.EFFECT_TICK,
            fallbackDuration = 20L,
            fallbackAmplitude = 80
        )
    }

    actual fun click() {
        vibrate(
            primitive = VibrationEffect.Composition.PRIMITIVE_CLICK,
            primitiveScale = 0.6f,
            predefined = VibrationEffect.EFFECT_CLICK,
            fallbackDuration = 30L,
            fallbackAmplitude = 150
        )
    }

    actual fun heavyClick() {
        vibrate(
            primitive = VibrationEffect.Composition.PRIMITIVE_THUD,
            primitiveScale = 1.0f,
            predefined = VibrationEffect.EFFECT_HEAVY_CLICK,
            fallbackDuration = 50L,
            fallbackAmplitude = 255
        )
    }

    private fun vibrate(
        primitive: Int,
        primitiveScale: Float,
        predefined: Int,
        fallbackDuration: Long,
        fallbackAmplitude: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator.arePrimitivesSupported(primitive).all { it }
        ) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(primitive, primitiveScale)
                    .compose()
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(predefined))
        } else {
            vibrator.vibrate(
                VibrationEffect.createOneShot(fallbackDuration, fallbackAmplitude)
            )
        }
    }
}
