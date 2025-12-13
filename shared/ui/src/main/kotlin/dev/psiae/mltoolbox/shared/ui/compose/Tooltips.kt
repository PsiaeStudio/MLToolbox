package dev.psiae.mltoolbox.shared.ui.compose

import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme

@Composable
fun SimpleTooltip(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    TooltipArea(
        delayMillis = 300,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Default),
        tooltip = {
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 24.dp)
                    .background(Material3Theme.colorScheme.inverseSurface)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text,
                    color = Material3Theme.colorScheme.inverseOnSurface,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = maxLines
                )
            }
        }
    ) {
        content()
    }
}