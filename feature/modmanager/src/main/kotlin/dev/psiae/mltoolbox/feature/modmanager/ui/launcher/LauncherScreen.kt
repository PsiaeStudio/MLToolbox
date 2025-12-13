package dev.psiae.mltoolbox.feature.modmanager.ui.launcher

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.ui.ModManagerScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenModel
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState

data object LauncherScreen : ModManagerScreen(
    "launcher"
) {

    @Composable
    operator fun invoke(modManagerScreenState: ModManagerScreenState) =
        dev.psiae.mltoolbox.feature.modmanager.ui.compose.launcher.LauncherScreen(modManagerScreenState)
}