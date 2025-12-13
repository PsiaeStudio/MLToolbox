package dev.psiae.mltoolbox.feature.modmanager.ui.compose.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.foundation.ui.compose.ScreenState

@Composable
fun rememberDashboardScreenState(
    model: DashboardScreenModel
): DashboardScreenState {

    return remember(model) { DashboardScreenState(model) }
}

class DashboardScreenState(
    val model: DashboardScreenModel
) : ScreenState(model.context) {

    val fs = model.fs
    val gameContext
        get() = model.modManagerScreenState.gameContext
}