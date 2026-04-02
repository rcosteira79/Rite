package com.ricardocosteira.habitlock.presentation.ui.haptics

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual class HapticController {

    actual fun tick() {
        val generator =
            UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
        generator.prepare()
        generator.impactOccurred()
    }

    actual fun click() {
        val generator =
            UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
        generator.prepare()
        generator.impactOccurred()
    }

    actual fun heavyClick() {
        val generator =
            UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
        generator.prepare()
        generator.impactOccurred()
    }
}
