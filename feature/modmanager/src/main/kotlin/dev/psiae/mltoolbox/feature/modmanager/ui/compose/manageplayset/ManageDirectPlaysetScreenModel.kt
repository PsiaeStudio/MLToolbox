package dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberManageDirectPlaysetScreenModel(
    managePlaysetScreenModel: ManagePlaysetScreenModel
): ManageDirectPlaysetScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel(managePlaysetScreenModel) { ManageDirectPlaysetScreenModel(managePlaysetScreenModel, screenContext) }
}

class ManageDirectPlaysetScreenModel(
    val managePlaysetScreenModel: ManagePlaysetScreenModel,
    context: ScreenContext,
) : ScreenModel(context) {

    val fs = FileSystem.SYSTEM
}