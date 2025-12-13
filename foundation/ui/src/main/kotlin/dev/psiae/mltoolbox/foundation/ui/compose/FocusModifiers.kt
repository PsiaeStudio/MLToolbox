package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties

private val ClosedFocusGateModifier = Modifier.focusProperties {
    canFocus = false
    onEnter = {
        cancelFocusChange()
    }
}

fun Modifier.focusGate(open: Boolean) = thenIf(!open) {
    ClosedFocusGateModifier
}