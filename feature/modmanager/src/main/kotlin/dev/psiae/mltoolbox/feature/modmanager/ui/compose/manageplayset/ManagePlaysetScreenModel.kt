package dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberManagePlaysetScreenModel(
    modManagerScreenModel: ModManagerScreenState
): ManagePlaysetScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel(modManagerScreenModel) { ManagePlaysetScreenModel(modManagerScreenModel, screenContext) }
}

class ManagePlaysetScreenModel(
    val modManagerScreenModel: ModManagerScreenState,
    context: ScreenContext
) : ScreenModel(context) {

    val fs = FileSystem.SYSTEM
}