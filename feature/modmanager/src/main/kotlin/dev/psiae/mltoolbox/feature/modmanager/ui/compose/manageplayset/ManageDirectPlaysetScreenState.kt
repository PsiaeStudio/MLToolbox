package dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.nav.ScreenNavEntry

@Composable
fun rememberManageDirectPlaysetScreenState(
    screenModel: ManageDirectPlaysetScreenModel
): ManageDirectPlaysetScreenState {
    val componentContext = LocalScreenContext.current
    return remember(screenModel) {
        ManageDirectPlaysetScreenState(screenModel)
    }
}

class ManageDirectPlaysetScreenState(
    val model: ManageDirectPlaysetScreenModel
) {
    val navigator = Navigator(this)

    class Navigator(
        val screen: ManageDirectPlaysetScreenState
    ) {
        val stack = mutableStateListOf<ScreenNavEntry>()
    }
}