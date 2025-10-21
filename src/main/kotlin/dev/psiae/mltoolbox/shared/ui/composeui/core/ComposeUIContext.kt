package dev.psiae.mltoolbox.shared.ui.composeui.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

abstract class ComposeUIContext {

    abstract val dispatchContext: UIDispatchContext
    abstract val lifetime : Job

    abstract fun newUICoroutineScope(): CoroutineScope
}

class ComposeUIContextImpl(
    override val dispatchContext: UIDispatchContext,
    override val lifetime : Job
) : ComposeUIContext() {

    override fun newUICoroutineScope(): CoroutineScope {
        return CoroutineScope(
            dispatchContext.mainDispatcher.immediate + SupervisorJob(lifetime)
        )
    }
}