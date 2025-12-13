package dev.psiae.mltoolbox.feature.modmanager.ui.managemods

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.ui.ModManagerScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenModel
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState

data object ManageModsScreen : ModManagerScreen(
    "manage_mods"
) {
    @Composable
    operator fun invoke(modManagerScreenModel: ModManagerScreenState) =
        dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods.ManageModsScreen(modManagerScreenModel)
}