package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainDrawerDestination

@Composable
fun modManagerMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { ModManagerMainScreen() }
    val painter = painterResource("drawable/icon_ios_glyph_gears_100px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "MODS",
            icon = painter,
            iconTint = null,
            name = "Mod Manager",
            content = content
        )
    }
}