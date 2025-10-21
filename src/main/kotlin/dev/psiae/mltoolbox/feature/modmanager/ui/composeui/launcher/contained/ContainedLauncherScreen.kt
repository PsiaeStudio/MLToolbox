package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.launcher.contained

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.WIPScreen
import dev.psiae.mltoolbox.feature.modmanager.launcher.ui.LauncherScreenState

@Composable
fun ContainedLauncherScreen(
    launcherScreenState: LauncherScreenState
) {
    val state = rememberContainedLauncherScreenState(launcherScreenState)
    WIPScreen()
}