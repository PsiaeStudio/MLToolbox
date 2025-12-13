package dev.psiae.mltoolbox.foundation.ui.compose.graphics

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import kotlin.math.ln

val Material3Theme.absoluteContentColor: Color
    @Composable
    get() = if (LocalIsDarkTheme.current)
        Color.White
    else
        Color.Black

@Composable
fun Material3Theme.surfaceColorAtElevation(
    elevation: Dp
): Color {
    val colorScheme = colorScheme
    return surfaceColorAtElevation(colorScheme.surface, colorScheme.surfaceTint, elevation)
}

fun Material3Theme.surfaceColorAtElevation(
    surface: Color,
    tint: Color,
    elevation: Dp
): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return tint.copy(alpha = alpha).compositeOver(surface)
}