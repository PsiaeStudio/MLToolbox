package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.launcher.contained

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.feature.modmanager.launcher.ui.LauncherScreenState
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberContainedLauncherScreenState(
    launcherScreenState: LauncherScreenState
): ContainedLauncherScreenState {
    val uiContext = LocalComposeUIContext.current
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
    private val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var selectedTab by mutableStateOf("shared")

    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

    }
}