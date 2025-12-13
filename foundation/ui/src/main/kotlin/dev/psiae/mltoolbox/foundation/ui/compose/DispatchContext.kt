package dev.psiae.mltoolbox.foundation.ui.compose

import dev.psiae.mltoolbox.core.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher

interface DispatchContext {

    val mainDispatcher: MainCoroutineDispatcher
    val ioDispatcher: CoroutineDispatcher
}

class DispatchContextImpl(
    override val mainDispatcher: MainCoroutineDispatcher,
    override val ioDispatcher: CoroutineDispatcher,
) : DispatchContext {

}