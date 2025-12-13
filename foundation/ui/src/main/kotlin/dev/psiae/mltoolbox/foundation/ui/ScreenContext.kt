package dev.psiae.mltoolbox.foundation.ui

import dev.psiae.mltoolbox.foundation.ui.compose.DispatchContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

interface ScreenContext : ComponentContext

/*internal*/ class ScreenContextImpl(
    override val dispatch: DispatchContext,
    override val lifetime : Job
) : ScreenContext {

    override fun createCoroutineScope(): CoroutineScope {
        return CoroutineScope(
            dispatch.mainDispatcher.immediate + SupervisorJob(lifetime)
        )
    }
}