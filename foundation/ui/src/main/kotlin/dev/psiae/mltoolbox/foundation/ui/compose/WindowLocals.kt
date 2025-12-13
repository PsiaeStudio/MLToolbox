package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.runtime.staticCompositionLocalOf
import java.awt.Window

// LocalWindow
val LocalAwtWindow = staticCompositionLocalOf<Window> {
    compositionLocalNotProvidedError("LocalWindow not provided")
}