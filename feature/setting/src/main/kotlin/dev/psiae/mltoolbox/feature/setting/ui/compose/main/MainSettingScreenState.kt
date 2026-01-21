package dev.psiae.mltoolbox.feature.setting.ui.compose.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.feature.setting.ui.main.MainSettingScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.ScreenState
import dev.psiae.mltoolbox.foundation.ui.nav.ScreenNavEntry

@Composable
fun rememberMainSettingScreenState(
    model: MainSettingScreenModel = rememberMainSettingScreenModel()
): MainSettingScreenState {
    return remember {
        MainSettingScreenState(model)
    }
}

class MainSettingScreenState(
    val model: MainSettingScreenModel
) : ScreenState(
    model.context
) {

    val navigator = Navigator(this)


    class Navigator(
        screenState: MainSettingScreenState
    ) {
        val stack = mutableStateListOf<ScreenNavEntry>()
    }
}