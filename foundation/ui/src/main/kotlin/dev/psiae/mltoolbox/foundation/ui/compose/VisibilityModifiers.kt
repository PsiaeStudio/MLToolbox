package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints

private object CollapsedLayoutModifier : LayoutModifier {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        return layout(0, 0) {}
    }
}

private object InvisibleLayoutModifier : LayoutModifier {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {}
    }
}

fun Modifier.visibilityGone() = this then(
    CollapsedLayoutModifier
        .clearAndSetSemantics {}
)

fun Modifier.visibilityInvisible() = this then(
    InvisibleLayoutModifier
        .clearAndSetSemantics {}
)