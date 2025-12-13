package dev.psiae.mltoolbox.feature.modmanager.dashboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.shared.utils.runtimeError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberDashboardScreenState(
    modManagerScreenState: ModManagerScreenState
): DashboardScreenState {
    val uiContext = LocalScreenContext.current
    val state = remember(modManagerScreenState) {
        DashboardScreenState(modManagerScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class DashboardScreenState(
    val modManagerScreenState: ModManagerScreenState,
    val uiContext: ScreenContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var selectedTab by mutableStateOf("direct")

    val selectedPlatform by derivedStateOf { modManagerScreenState.gamePlatform }
    val selectedInstallFolder by derivedStateOf { modManagerScreenState.gameInstallDirectory?.absolutePath ?: "" }
    val selectedRootFolder by derivedStateOf { modManagerScreenState.gameRootDirectory?.absolutePath ?: "" }
    val selectedGameLauncherExe by derivedStateOf { modManagerScreenState.gameLauncherFile?.absolutePath ?: "" }
    val selectedGameBinaryExe by derivedStateOf { modManagerScreenState.gameBinaryFile?.absolutePath ?: "" }
    val selectedGamePaksFolder by derivedStateOf { modManagerScreenState.gamePaksFolder?.absolutePath ?: "" }
    val selectedGameVersion by derivedStateOf { modManagerScreenState.gameVersion }
    val selectedGameVersionCustom by derivedStateOf { modManagerScreenState.gameVersionCustom }

    fun platformDisplayName(): String = when (selectedPlatform) {
        "steam" -> "Steam"
        "xbox_pc_gamepass" -> "Xbox PC Game Pass"
        "gog_com" -> "GOG.com"
        "epic_games_store" -> "Epic Games Store"
        else -> runtimeError("Unknown platform: $selectedPlatform")
    }

    fun stateEnter() {
        _coroutineScope = uiContext.createCoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

    }
}