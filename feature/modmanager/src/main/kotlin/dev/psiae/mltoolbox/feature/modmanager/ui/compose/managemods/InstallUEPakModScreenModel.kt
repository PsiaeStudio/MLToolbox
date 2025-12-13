package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.foundation.domain.ConfigurationWarden
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberInstallUEPakModScreenModel(
    gameContext: ModManagerGameContext,
    configWarden: ConfigurationWarden = ConfigurationWarden.getInstance(),
    initializer: InstallUEPakModScreenModel.() -> Unit = {},
): InstallUEPakModScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel(configWarden) {
        InstallUEPakModScreenModel(screenContext, gameContext, configWarden)
            .apply(initializer)
    }
}

class InstallUEPakModScreenModel(
    context: ScreenContext,
    val gameContext: ModManagerGameContext,
    val configWarden: ConfigurationWarden
) : ScreenModel(
    context
) {

}