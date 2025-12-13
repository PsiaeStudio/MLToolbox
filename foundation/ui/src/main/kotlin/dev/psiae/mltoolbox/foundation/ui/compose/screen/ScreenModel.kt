package dev.psiae.mltoolbox.foundation.ui.compose.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.foundation.ui.ScreenModel

@Composable
fun <T: ScreenModel> rememberScreenModel(
    vararg keys: Any?,
    calculation: @DisallowComposableCalls () -> T
): T {
    val rememberObserver = remember(*keys) {
        ComposeScreenModel(calculation())
    }
    @Suppress("UNCHECKED_CAST")
    return rememberObserver.screenModel as T
}

private class ComposeScreenModel(
    val screenModel: ScreenModel
) : RememberObserver {

    override fun onAbandoned() {
        screenModel.onAbandoned()
    }

    override fun onForgotten() {
        screenModel.onForgotten()
    }

    override fun onRemembered() {
        screenModel.onRemembered()
    }
}