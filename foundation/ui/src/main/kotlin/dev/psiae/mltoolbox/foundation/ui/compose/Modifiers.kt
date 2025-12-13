package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.ui.Modifier

inline fun Modifier.thenIf(
    condition: Boolean,
    block: () -> Modifier
): Modifier {
    if (!condition)
        return this
    return this.then(block())
}