package dev.psiae.mltoolbox.feature.modmanager.ui.compose.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.Closeable
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.domain.ConfigurationGuard
import dev.psiae.mltoolbox.foundation.domain.ConfigurationWarden
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel
import dev.psiae.mltoolbox.shared.domain.model.GamePlatform

@Composable
fun rememberDashboardScreenModel(
    modManagerScreenState: ModManagerScreenState,
    configurationWarden: ConfigurationWarden = ConfigurationWarden.getInstance()
): DashboardScreenModel {
    val screenContext = LocalScreenContext.current
    val state = rememberScreenModel(modManagerScreenState, configurationWarden) {
        DashboardScreenModel(modManagerScreenState, configurationWarden, screenContext)
    }
    return state
}

class DashboardScreenModel(
    val modManagerScreenState: ModManagerScreenState,
    val configWarden: ConfigurationWarden,
    context: ScreenContext
): ScreenModel(context) {

    private var _closeables = mutableListOf<Closeable>()


    var selectedTab by mutableStateOf("direct")

    val selectedPlatform by derivedStateOf { modManagerScreenState.gameContext?.platform }
    val selectedInstallFolder by derivedStateOf { modManagerScreenState.gameContext?.paths?.install }
    val selectedRootFolder by derivedStateOf { modManagerScreenState.gameContext?.paths?.root }
    val selectedGameLauncherExe by derivedStateOf { modManagerScreenState.gameContext?.paths?.launcher }
    val selectedGameBinaryExe by derivedStateOf { modManagerScreenState.gameContext?.paths?.binary }
    val selectedGamePaksFolder by derivedStateOf { modManagerScreenState.gameContext?.paths?.paks }
    val selectedGameVersion by derivedStateOf{ modManagerScreenState.gameContext?.version }

    val fs = FileSystem.SYSTEM

    fun platformDisplayName(): String = when (val selectedPlatform = selectedPlatform) {
        is GamePlatform -> selectedPlatform.label
        else -> ""
    }

    fun init() {

    }

    override fun onForgotten() {
        _closeables.forEach(Closeable::close)
    }

    fun editGameContext() {
        val guard = configWarden.placeGuardIf(
            setOf(ModManagerGameContext),
            { key, guards ->
                guards.isEmpty()
            },
            {
                ConfigurationGuard(
                    "Dashboard Edit Game Context",
                    "Editing game context",
                    isEditing = true
                )
            }
        )
        if (guard != null) {
            _closeables.add(
                object : Closeable {
                    override fun close() {
                        configWarden.removeGuard(guard)
                    }
                }
            )
            modManagerScreenState.redoGameContext()
        }
    }
}