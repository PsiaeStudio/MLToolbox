package dev.psiae.mltoolbox.feature.gamemanager.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainDrawerDestination

@Composable
fun gameManagerMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { GameManagerScreen() }
    val painter = painterResource("drawable/icon_game_manager_24px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "game_manager",
            icon = painter,
            iconTint = null,
            name = "Game Manager",
            content = content
        )
    }
}