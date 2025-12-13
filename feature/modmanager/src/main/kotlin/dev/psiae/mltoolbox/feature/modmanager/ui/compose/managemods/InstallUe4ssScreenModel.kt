package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberInstallUe4ssScreenModel(
    gameContext: ModManagerGameContext,
    initializer: InstallUe4ssScreenModel.() -> Unit = {}
): InstallUe4ssScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel(gameContext) {
        InstallUe4ssScreenModel(screenContext, gameContext)
            .apply(initializer)
    }
}

class InstallUe4ssScreenModel(
    context: ScreenContext,
    val gameContext: ModManagerGameContext
) : ScreenModel(
    context
) {

}