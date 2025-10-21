package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.managed

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.WIPScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.ManageModsScreenState

@Composable
fun ContainedLauncherScreen(
    manageModsScreenState: ManageModsScreenState
) {
    val state = rememberContainedLauncherScreenState(manageModsScreenState)
    WIPScreen()
}