package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenModel
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState

@Composable
internal fun ManageModsScreen(
    modManagerScreenModel: ModManagerScreenState
) {
    val model = rememberManageModsScreenModel(modManagerScreenModel)
    ManageModsScreen(model)
}

@Composable
private fun ManageModsScreen(
    screenModel: ManageModsScreenModel
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            ManageDirectModsScreen(screenModel)
        }
    }
}