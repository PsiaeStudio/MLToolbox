package dev.psiae.mltoolbox.foundation.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/**
 * [Screen] Model
 */
abstract class ScreenModel(
    val context: ScreenContext
) {
    private var _coroutineScope: CoroutineScope? = null
    val coroutineScope: CoroutineScope
        get() = checkNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    private var remembered = false

    open fun onRemembered() {
        if (remembered)
            error("onRemembered called twice")
        remembered = true
        _coroutineScope = context.createCoroutineScope()
    }

    open fun onForgotten() {
        if (!remembered)
            error("onForgotten called but was not remembered")
        coroutineScope.cancel()
    }

    open fun onAbandoned() {
        if (remembered)
            error("onAbandoned called but was remembered")
    }
}