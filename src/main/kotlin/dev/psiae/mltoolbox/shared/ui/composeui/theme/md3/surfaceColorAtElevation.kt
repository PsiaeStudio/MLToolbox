package dev.psiae.mltoolbox.shared.ui.composeui.theme.md3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import kotlin.math.ln


@Composable
fun MD3Theme.surfaceColorAtElevation(
    surface: Color,
    tint: Color,
    elevation: Dp,
): Color {
    if (elevation == 0.dp) return surface
    return remember(surface, tint, elevation) {
        val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
        tint.copy(alpha = alpha).compositeOver(surface)
    }
}