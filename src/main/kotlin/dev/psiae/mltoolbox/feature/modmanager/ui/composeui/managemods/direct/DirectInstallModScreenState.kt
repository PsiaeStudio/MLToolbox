package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberDirectInstallModScreenState(
    manageDirectModsScreenState: ManageDirectModsScreenState
): DirectInstallModScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(manageDirectModsScreenState) {
        DirectInstallModScreenState(manageDirectModsScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        manageDirectModsScreenState.installModScreenEnter(state)
        onDispose {
            state.stateExit()
            manageDirectModsScreenState.installModScreenExit(state)
        }
    }

    return state
}

class DirectInstallModScreenState(
    val manageDirectModsScreenState: ManageDirectModsScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    var navigateToInstallUE4SS by mutableStateOf(false)
        private set

    var navigateToInstallUE4SSMod by mutableStateOf(false)
        private set

    var navigateToInstallUnrealEngineMod by mutableStateOf(false)
        private set

    fun init() {

    }

    fun userInputExit() {
        manageDirectModsScreenState.userInputNavigateOutInstallModScreen()
    }

    fun userInputNavigateToInstallUE4SS() {
        navigateToInstallUE4SS = true
    }

    fun userInputNavigateOutInstallUE4SS() {
        navigateToInstallUE4SS = false
    }

    fun userInputNavigateToInstallUE4SSMod() {
        navigateToInstallUE4SSMod = true
    }

    fun userInputNavigateOutInstallUE4SSMod() {
        navigateToInstallUE4SSMod = false
    }

    fun userInputNavigateToInstallUnrealEngineMod() {
        navigateToInstallUnrealEngineMod = true
    }

    fun userInputNavigateOutInstallUnrealEngineMod() {
        navigateToInstallUnrealEngineMod = false
    }
}