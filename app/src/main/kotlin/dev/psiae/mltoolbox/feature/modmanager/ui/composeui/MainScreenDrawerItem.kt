package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreen
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainDrawerDestination

@Composable
fun modManagerMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { ModManagerScreen() }
    val painter = painterResource("drawable/icon_modmanager_nut_outline_24px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "modmanager",
            icon = painter,
            iconTint = null,
            name = "Mod Manager",
            content = content
        )
    }
}