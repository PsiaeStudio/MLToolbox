package dev.psiae.mltoolbox.shared.ui.compose.core.window.locals

import androidx.compose.runtime.staticCompositionLocalOf
import dev.psiae.mltoolbox.foundation.ui.compose.staticCompositionLocalNotProvidedError
import dev.psiae.mltoolbox.shared.ui.compose.core.window.PlatformWindow

val LocalPlatformWindow = staticCompositionLocalOf<PlatformWindow> {
    staticCompositionLocalNotProvidedError("LocalPlatformWindow")
}