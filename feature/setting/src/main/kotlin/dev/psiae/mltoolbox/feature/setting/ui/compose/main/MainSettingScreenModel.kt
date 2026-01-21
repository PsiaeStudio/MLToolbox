package dev.psiae.mltoolbox.feature.setting.ui.compose.main

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.setting.ui.main.MainSettingScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberMainSettingScreenModel(

): MainSettingScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel {
        MainSettingScreenModel(screenContext)
    }
}