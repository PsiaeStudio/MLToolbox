package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.runtime.RememberObserver
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

abstract class ScreenState(
    protected val context: ScreenContext
) : RememberObserver {

    private var _coroutineScope: CoroutineScope? = null
    val coroutineScope: CoroutineScope
        get() = checkNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    private var remembered = false

    override fun onRemembered() {
        if (remembered)
            error("onRemembered called twice")
        remembered = true
        _coroutineScope = context.createCoroutineScope()
    }

    override fun onForgotten() {
        if (!remembered)
            error("onForgotten called but was not remembered")
        coroutineScope.cancel()
    }

    override fun onAbandoned() {
        if (remembered)
            error("onAbandoned called but was remembered")
    }
}