package dev.psiae.mltoolbox.shared.ui.composeui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpace
import kotlin.math.round

fun Color.brighten(times: Float) = copy(
    red = round(red.times(times))
        .coerceIn(colorSpace.componentRange(0)),
    green = round(green.times(times))
        .coerceIn(colorSpace.componentRange(1)),
    blue = round(blue.times(times))
        .coerceIn(colorSpace.componentRange(2)),
)

private fun ColorSpace.componentRange(component: Int) = getMinValue(component)..getMaxValue(component)