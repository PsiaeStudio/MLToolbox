package dev.psiae.mltoolbox.shared.ui.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.psiae.mltoolbox.foundation.ui.compose.compositionLocalNotProvidedError

val LocalTitleBarBehavior = staticCompositionLocalOf<TitleBarBehavior> {
    compositionLocalNotProvidedError(
        "TitleBarBehavior "
    )
}

@Stable
interface TitleBarBehavior {
    val showRestoreWindow: Boolean
    val titleBarHeightPx: Int

    fun minimizeClicked()
    fun restoreClicked()
    fun maximizeClicked()
    fun closeClicked()
}