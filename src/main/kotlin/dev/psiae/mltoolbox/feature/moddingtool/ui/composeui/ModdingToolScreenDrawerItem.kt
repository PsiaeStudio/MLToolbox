package dev.psiae.mltoolbox.feature.moddingtool.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainDrawerDestination

@Composable
fun modToolScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { ModToolScreen() }
    val painter = painterResource("drawable/icon_modding_tool_24px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "modding_tool",
            icon = painter,
            iconTint = null,
            name = "Modding Tool",
            content = content
        )
    }
}