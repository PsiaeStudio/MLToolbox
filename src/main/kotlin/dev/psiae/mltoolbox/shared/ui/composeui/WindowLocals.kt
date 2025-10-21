package dev.psiae.mltoolbox.shared.ui.composeui

import androidx.compose.runtime.staticCompositionLocalOf
import java.awt.Window

// LocalWindow
val LocalAwtWindow = staticCompositionLocalOf<Window> {
    compositionLocalNotProvidedError("LocalWindow not provided")
}