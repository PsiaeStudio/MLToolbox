package dev.psiae.mltoolbox.feature.modmanager.ui.compose.setup

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel

@Composable
fun rememberSetupModManagerScreenModel(
    modManagerScreenState: ModManagerScreenState
): SetupModManagerScreenModel {
    val uiContext = LocalScreenContext.current
    val state = rememberScreenModel(modManagerScreenState) {
        SetupModManagerScreenModel(modManagerScreenState, uiContext)
    }
    return state
}

class SetupModManagerScreenModel(
    private val modManagerScreenState: ModManagerScreenState,
    context: ScreenContext,
) : ScreenModel(
    context
) {
    override fun onRemembered() {
        super.onRemembered()
        init()
    }
    private fun init() {
        modManagerScreenState
    }


    fun selectGameContext(
        gameContext: ModManagerGameContext
    ) {
        modManagerScreenState.selectGameContext(gameContext)
    }
}