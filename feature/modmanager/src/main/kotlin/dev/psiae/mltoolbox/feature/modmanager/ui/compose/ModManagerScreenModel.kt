package dev.psiae.mltoolbox.feature.modmanager.ui.compose

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.foundation.domain.ConfigurationWarden
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberModManagerScreenModel(
    configWarden: ConfigurationWarden = ConfigurationWarden.getInstance()
): ModManagerScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel(configWarden) {
        ModManagerScreenModel(screenContext, configWarden)
    }
}

class ModManagerScreenModel(
    context: ScreenContext,
    val configWarden: ConfigurationWarden,
) : ScreenModel(
    context = context,
) {

}