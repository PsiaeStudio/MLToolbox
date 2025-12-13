package dev.psiae.mltoolbox.shared.ui.compose.theme.md3

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme

@Composable
fun MD3Theme.ripple() = androidx.compose.material3.ripple(color = Material3Theme.colorScheme.onSecondaryContainer)