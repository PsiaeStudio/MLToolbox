package dev.psiae.mltoolbox.feature.modmanager.ui.dashboard

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.ui.ModManagerScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenModel
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState

data object DashboardScreen : ModManagerScreen(
    "dashboard"
) {

    @Composable
    operator fun invoke(modManagerScreenState: ModManagerScreenState) =
        dev.psiae.mltoolbox.feature.modmanager.ui.compose.dashboard.DashboardScreen(modManagerScreenState)
}