package dev.psiae.mltoolbox.shared.ui.composeui.core

import kotlinx.coroutines.MainCoroutineDispatcher

abstract class UIDispatchContext {

    abstract val mainDispatcher: MainCoroutineDispatcher
}

class UIDispatchContextImpl(
    override val mainDispatcher: MainCoroutineDispatcher
) : UIDispatchContext() {

}