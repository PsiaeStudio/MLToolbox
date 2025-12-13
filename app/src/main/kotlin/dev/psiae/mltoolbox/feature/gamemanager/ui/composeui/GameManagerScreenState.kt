package dev.psiae.mltoolbox.feature.gamemanager.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.foundation.ui.ComponentContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext

@Composable
fun rememberGameManagerScreenState(): GameManagerScreenState {
    val componentContext = LocalScreenContext.current
    val state = remember {
        GameManagerScreenState(componentContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class GameManagerScreenState(
    val componentContext: ComponentContext
) {

    fun stateEnter() {}
    fun stateExit() {}
}