package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.launcher.contained

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.feature.modmanager.launcher.ui.LauncherScreenState
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberContainedLauncherScreenState(
    launcherScreenState: LauncherScreenState
): ContainedLauncherScreenState {
    val uiContext = LocalScreenContext.current
    val state = remember(launcherScreenState) {
        ContainedLauncherScreenState(launcherScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class ContainedLauncherScreenState(
    private val launcherScreenState: LauncherScreenState,
    private val uiContext: ScreenContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var selectedTab by mutableStateOf("shared")

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