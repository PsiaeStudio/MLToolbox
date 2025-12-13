package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.runtime.staticCompositionLocalOf
import dev.psiae.mltoolbox.foundation.ui.ComponentContext
import dev.psiae.mltoolbox.foundation.ui.ScreenContext

val LocalScreenContext = staticCompositionLocalOf<ScreenContext> {
    staticCompositionLocalNotProvidedError("LocalScreenContext")
}
val LocalComponentContext = staticCompositionLocalOf<ComponentContext> {
    staticCompositionLocalNotProvidedError("LocalComponentContext")
}