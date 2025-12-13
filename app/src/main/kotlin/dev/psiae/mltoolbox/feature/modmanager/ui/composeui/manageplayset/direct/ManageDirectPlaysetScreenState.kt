package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.manageplayset.direct

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.manageplayset.ManagePlaysetScreenState
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberManageDirectPlaysetScreenState(
    managePlaysetScreenState: ManagePlaysetScreenState
): ManageDirectPlaysetScreenState {
    val uiContext = LocalScreenContext.current
    val state = remember(managePlaysetScreenState) {
        ManageDirectPlaysetScreenState(managePlaysetScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class ManageDirectPlaysetScreenState(
    val managePlaysetScreenState: ManagePlaysetScreenState,
    val uiContext: ScreenContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    fun stateEnter() {
        _coroutineScope = uiContext.createCoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

    }
}