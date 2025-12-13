package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.manageplayset

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberManagePlaysetScreenState(
    modManagerScreenState: ModManagerScreenState
): ManagePlaysetScreenState {
    val uiContext = LocalScreenContext.current
    val state = remember(modManagerScreenState) {
        ManagePlaysetScreenState(modManagerScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class ManagePlaysetScreenState(
    val modManagerScreenState: ModManagerScreenState,
    val uiContext: ScreenContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var selectedTab by mutableStateOf("direct")

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