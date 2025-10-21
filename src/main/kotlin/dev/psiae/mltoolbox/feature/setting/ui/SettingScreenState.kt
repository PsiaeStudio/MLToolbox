package dev.psiae.mltoolbox.feature.setting.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainScreenState
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun rememberSettingScreenState(
    mainScreenState: MainScreenState
): SettingScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(mainScreenState) {
        SettingScreenState(uiContext, mainScreenState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class SettingScreenState(
    val uiContext: ComposeUIContext,
    val mainState: MainScreenState
) {
    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    val userProfile by derivedStateOf { mainState.userProfile }
    val userProfileSetting by derivedStateOf { mainState.userProfileSetting }

    fun userSelectColorMode(colorMode: String) {
        val uid = userProfile?.uuid?.toString() ?: return
        mainState.userService.changeUserProfileColorMode(uid, colorMode)
    }

    fun userSelectColorSeed(colorSeed: String) {
        val uid = userProfile?.uuid?.toString() ?: return
        mainState.userService.changeUserProfileColorSeed(uid, colorSeed)
    }

    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

        coroutineScope.launch {
        }
    }

    fun userInputExitSetting() {
        mainState.userInputCloseSetting()
    }
}