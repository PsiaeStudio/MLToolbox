package dev.psiae.mltoolbox.feature.supportproject.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainDrawerDestination

@Composable
fun supportProjectMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { DonateMainScreen() }
    val painter = painterResource("drawable/icon_support_project_handshake_24px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "support_project",
            icon = painter,
            iconTint = null,
            name = "Support Project",
            content = content
        )
    }
}