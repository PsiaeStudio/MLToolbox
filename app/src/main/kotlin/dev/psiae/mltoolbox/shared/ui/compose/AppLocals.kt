package dev.psiae.mltoolbox.shared.ui.compose

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ApplicationScope
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.foundation.ui.compose.compositionLocalNotProvidedError

val LocalApplication = staticCompositionLocalOf<MLToolboxApp> {
    compositionLocalNotProvidedError("LocalApplication")
}

val LocalComposeApplicationScope = staticCompositionLocalOf<ApplicationScope> {
    compositionLocalNotProvidedError("LocalComposeApplicationScope")
}