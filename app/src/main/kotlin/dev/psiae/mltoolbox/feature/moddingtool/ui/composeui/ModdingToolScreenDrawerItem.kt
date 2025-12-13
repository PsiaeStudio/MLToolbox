package dev.psiae.mltoolbox.feature.moddingtool.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainDrawerDestination

@Composable
fun modForgeScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { ModToolScreen() }
    val painter = painterResource("drawable/icon_forge_anvil_outline_24px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "forge",
            icon = painter,
            iconTint = null,
            name = "Forge",
            content = content
        )
    }
}