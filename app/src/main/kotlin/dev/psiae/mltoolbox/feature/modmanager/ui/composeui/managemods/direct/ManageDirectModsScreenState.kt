package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.ManageModsScreenState
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberManageDirectModsScreenState(
    manageModsScreenState: ManageModsScreenState
): ManageDirectModsScreenState {
    val uiContext = LocalScreenContext.current
    val state = remember(manageModsScreenState) {
        ManageDirectModsScreenState(manageModsScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class ManageDirectModsScreenState(
    val manageModsScreenState: ManageModsScreenState,
    val uiContext: ScreenContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var installModDestination by mutableStateOf(false)
        private set
    var uninstallModDestination by mutableStateOf(false)
        private set

    var installModScreen by mutableStateOf<DirectInstallModScreenState?>(null)
        private set

    var uninstallModScreen by mutableStateOf<DirectInstallModScreenState?>(null)
        private set

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

    fun installModScreenEnter(screenState: DirectInstallModScreenState) {
        installModScreen = screenState
    }

    fun installModScreenExit(screenState: DirectInstallModScreenState) {
        if (installModScreen == screenState) {
            installModScreen = null
        }
    }

    fun userInputNavigateToInstallModScreen() {
        installModDestination = true
    }

    fun userInputNavigateOutInstallModScreen() {
        installModDestination = false
    }
}