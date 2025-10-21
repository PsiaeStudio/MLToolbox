package dev.psiae.mltoolbox.shared.ui.composeui.core.window.locals

import androidx.compose.runtime.staticCompositionLocalOf
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.staticCompositionLocalNotProvidedFor
import dev.psiae.mltoolbox.shared.ui.composeui.core.window.PlatformWindow

val LocalPlatformWindow = staticCompositionLocalOf<PlatformWindow> {
    staticCompositionLocalNotProvidedFor("LocalPlatformWindow")
}