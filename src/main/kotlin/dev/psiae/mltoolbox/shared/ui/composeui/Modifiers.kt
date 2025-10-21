package dev.psiae.mltoolbox.shared.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.layout

@Composable
fun Modifier.collapsible(collapsed: Boolean) = then(
    Modifier.layout { measurable, constraints ->
        if (collapsed) {
            layout(0, 0) {}
        } else {
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        }
    }
)

@Composable
fun Modifier.focusGate(open: Boolean) = then(
    Modifier.focusProperties {
        if (!open) {
            canFocus = false
            onEnter = {
                cancelFocusChange()
            }
        }
    }
)