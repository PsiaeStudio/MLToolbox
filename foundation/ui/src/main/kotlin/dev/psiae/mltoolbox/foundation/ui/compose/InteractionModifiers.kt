package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

private val InertModifier = Modifier
    // block focus from entering
    .focusGate(false)
    // block accessibility service
    .clearAndSetSemantics{}
    // block pointer events such as clicks
    .pointerInput(Unit) {
        val handlerCoroutineContext = currentCoroutineContext()
        awaitPointerEventScope {
            while (handlerCoroutineContext.isActive) {
                awaitPointerEvent(PointerEventPass.Initial).changes.fastForEach(PointerInputChange::consume)
            }
        }
    }
    // later: figure out how to block hover, could just render a blocking composable

fun Modifier.inert() = this then InertModifier