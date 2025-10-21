package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.shared.modmanager.ModManager
import kotlinx.coroutines.*

@Composable
fun rememberModManager(
    modManager: ModManager
): ModManagerComposeState  {
    val uiContext = LocalComposeUIContext.current
    val manager = remember(modManager, uiContext) {
        ModManagerComposeState(modManager, uiContext)
    }
    DisposableEffect(manager) {
        manager.stateEnter()
        onDispose {
            manager.stateExit()
        }
    }
    return manager
}

@Stable
class ModManagerComposeState(
    val modManager: ModManager,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var ready by mutableStateOf(false)
        private set



    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {
        coroutineScope.launch(uiContext.dispatchContext.mainDispatcher.immediate) {
            ready = true
        }
    }
}