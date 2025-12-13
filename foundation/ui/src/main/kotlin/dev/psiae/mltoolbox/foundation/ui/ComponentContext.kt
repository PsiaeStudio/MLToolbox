package dev.psiae.mltoolbox.foundation.ui

import dev.psiae.mltoolbox.foundation.ui.compose.DispatchContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface ComponentContext {

    val dispatch: DispatchContext
    val lifetime : Job

    fun createCoroutineScope(): CoroutineScope
}