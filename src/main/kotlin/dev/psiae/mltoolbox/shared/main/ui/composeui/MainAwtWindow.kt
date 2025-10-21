package dev.psiae.mltoolbox.shared.main.ui.composeui

import androidx.compose.ui.window.ApplicationScope
import javax.swing.JFrame
import com.sun.jna.Platform as JnaPlatform

open class MainAwtWindow protected constructor() : JFrame() {
}

fun ApplicationScope.MainAwtWindow(): MainAwtWindow {
    return when {
        JnaPlatform.getOSType() == JnaPlatform.WINDOWS -> WindowsMainAwtWindow(this)
        else -> TODO("No Impl for platform code=${JnaPlatform.getOSType()}")
    }
}

