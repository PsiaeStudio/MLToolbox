package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import kotlinx.coroutines.*

@Composable
fun rememberModManagerState(): ModManagerComposeState  {
    val uiContext = LocalScreenContext.current
    val manager = remember {
        ModManagerComposeState(uiContext)
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
    val uiContext: ScreenContext
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
        _coroutineScope = uiContext.createCoroutineScope()

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {
        coroutineScope.launch(uiContext.dispatch.mainDispatcher.immediate) {
            ready = true
        }
    }
}