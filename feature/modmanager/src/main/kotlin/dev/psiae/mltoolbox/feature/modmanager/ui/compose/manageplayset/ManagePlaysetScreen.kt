package dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState

@Composable
internal fun ManagePlaylistScreen(
    modManagerScreenModel: ModManagerScreenState
) {
    val model = rememberManagePlaysetScreenModel(modManagerScreenModel)
    ManagePlaylistScreen(model)
}

@Composable
private fun ManagePlaylistScreen(
    screenModel: ManagePlaysetScreenModel
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
            ManageDirectPlaysetScreen(screenModel)
        }
    }
}