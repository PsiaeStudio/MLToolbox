package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallModScreen
import dev.psiae.mltoolbox.foundation.ui.Screen
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.nav.NavEntryIdGenerator
import dev.psiae.mltoolbox.foundation.ui.nav.ScreenNavEntry

@Composable
fun rememberManageDirectModsScreenState(
    screenModel: ManageDirectModsScreenModel
): ManageDirectModsScreenState {
    val componentContext = LocalScreenContext.current
    return remember(screenModel) {
        ManageDirectModsScreenState(screenModel)
    }
}

class ManageDirectModsScreenState(
    val model: ManageDirectModsScreenModel
) {
    val navigator = Navigator(this)

    fun installMod() {
        navigator.navigateToInstallMod()
    }

    fun contentGoBack() {
        navigator.popScreen()
    }


    class Navigator(
        val screen: ManageDirectModsScreenState
    ) {
        val stack = mutableStateListOf<ScreenNavEntry>()

        fun navigateToScreen(
            screen: Screen
        ) {
            if (!stack.isEmpty()) {
                if (screen == stack.last())
                    return
            }
            stack.find { it.screen == screen }?.let {
                stack.remove(it)
            }
            stack.add(ScreenNavEntry(screen, NavEntryIdGenerator.generate(screen)))
        }
        fun popScreen() {
            if (!stack.isEmpty())
                stack.removeLast()
        }

        fun navigateToInstallMod() {
            navigateToScreen(InstallModScreen)
        }
    }
}