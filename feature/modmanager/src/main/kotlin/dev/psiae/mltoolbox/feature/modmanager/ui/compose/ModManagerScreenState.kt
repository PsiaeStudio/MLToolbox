package dev.psiae.mltoolbox.feature.modmanager.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.feature.modmanager.ui.ModManagerScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.browsemods.BrowseModsScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.dashboard.DashboardScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.launcher.LauncherScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.ManageModsScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.manageplayset.ManagePlaysetScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.setup.SetupScreen
import dev.psiae.mltoolbox.foundation.ui.compose.ScreenState
import kotlinx.coroutines.launch

@Composable
fun rememberModManagerScreenState(
    model: ModManagerScreenModel = rememberModManagerScreenModel()
): ModManagerScreenState {

    return remember(model) {
        ModManagerScreenState(model)
    }
}

class ModManagerScreenState(
    val model: ModManagerScreenModel
) : ScreenState(model.context) {

    val navigator = Navigator(this)

    var gameContext by mutableStateOf<ModManagerGameContext?>(null)
        private set

    var isNeedSetup by mutableStateOf<Boolean>(false)
        private set

    var isReady by mutableStateOf<Boolean>(false)
        private set

    val currentDrawerDestination
        get() = navigator.currentDrawerDestination

    override fun onRemembered() {
        super.onRemembered()
        init()
    }

    private fun init() {
        coroutineScope.launch {
            isNeedSetup = true
            isReady = true
        }
    }


    fun selectGameContext(gameContext: ModManagerGameContext) {
        this.gameContext = gameContext
        navigator.navigateToDashboard()
        isNeedSetup = false
    }

    fun redoGameContext() {
        this.gameContext = null
        isNeedSetup = true
    }

    class Navigator(
        private val screen: ModManagerScreenState
    ) {
        val stack = mutableStateListOf<String>()

        var currentDrawerDestination by mutableStateOf(DashboardScreen.name)
            private set

        private fun navigateToScreen(screen: ModManagerScreen) {
            stack.remove(screen.name)
            stack.add(screen.name)
            currentDrawerDestination = screen.name
        }

        fun navigateToSetup() {
            navigateToScreen(SetupScreen)
        }

        fun navigateToDashboard() {
            navigateToScreen(DashboardScreen)
        }
        fun navigateToBrowseMods() {
            navigateToScreen(BrowseModsScreen)
        }
        fun navigateToLauncher() {
            navigateToScreen(LauncherScreen)
        }
        fun navigateToManageMods() {
            navigateToScreen(ManageModsScreen)
        }
        fun navigateToManagePlayset() {
            navigateToScreen(ManagePlaysetScreen)
        }
    }
}