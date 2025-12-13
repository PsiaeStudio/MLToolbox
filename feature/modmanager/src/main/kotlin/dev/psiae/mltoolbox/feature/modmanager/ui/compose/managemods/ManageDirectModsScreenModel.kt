package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberManageDirectModsScreenModel(
    manageModsScreenModel: ManageModsScreenModel
): ManageDirectModsScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel(manageModsScreenModel) { ManageDirectModsScreenModel(manageModsScreenModel, screenContext) }
}

class ManageDirectModsScreenModel(
    val manageModsScreenModel: ManageModsScreenModel,
    context: ScreenContext,
) : ScreenModel(context) {

    val fs = FileSystem.SYSTEM
}