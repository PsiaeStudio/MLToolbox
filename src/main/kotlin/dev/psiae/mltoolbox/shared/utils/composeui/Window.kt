package dev.psiae.mltoolbox.shared.utils.composeui

import androidx.compose.ui.window.WindowScope
import java.awt.Window

inline fun <T> Window.windowScope(
    block: WindowScope.() -> T
): T {
    return object : WindowScope {
        override val window: Window
            get() = this@windowScope
    }.block()
}