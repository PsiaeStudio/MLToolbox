package dev.psiae.mltoolbox.feature.modmanager.ui.compose.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme

@Composable
internal fun SetupModManagerScreen(
    modManagerScreenState: ModManagerScreenState,
    modifier: Modifier = Modifier
) {
    val model = rememberSetupModManagerScreenModel(modManagerScreenState)
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = Material3Theme.colorScheme.surface
    ) {
        if (modManagerScreenState.isNeedSetup) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val setupState = rememberSetupGameContextPanelState(model)
                SetupGameContextPanel(setupState)
            }
            return@Surface
        }
    }
}