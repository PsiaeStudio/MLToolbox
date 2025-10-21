package dev.psiae.mltoolbox.shared.ui.composeui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ApplicationScope
import dev.psiae.mltoolbox.shared.app.MLToolboxApp

val LocalApplication = staticCompositionLocalOf<MLToolboxApp> {
    compositionLocalNotProvidedError("LocalApplication")
}

val LocalComposeApplicationScope = staticCompositionLocalOf<ApplicationScope> {
    compositionLocalNotProvidedError("LocalComposeApplicationScope")
}