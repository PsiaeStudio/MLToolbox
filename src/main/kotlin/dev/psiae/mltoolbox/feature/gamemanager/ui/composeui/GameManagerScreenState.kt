package dev.psiae.mltoolbox.feature.gamemanager.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext

@Composable
fun rememberGameManagerScreenState(): GameManagerScreenState {
    val composeUIContext = LocalComposeUIContext.current
    val state = remember {
        GameManagerScreenState(composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class GameManagerScreenState(
    val ComposeUIContext: ComposeUIContext
) {

    fun stateEnter() {}
    fun stateExit() {}
}