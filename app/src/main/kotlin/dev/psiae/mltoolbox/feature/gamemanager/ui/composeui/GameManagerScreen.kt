package dev.psiae.mltoolbox.feature.gamemanager.ui.composeui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.psiae.mltoolbox.shared.ui.compose.LocalApplication
import dev.psiae.mltoolbox.shared.ui.compose.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.feature.setting.personalization.ui.composeui.WIPScreen
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.compose.theme.md3.ripple
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme

@Composable
fun GameManagerScreen() {
    val state = rememberGameManagerScreenState()
    val app = LocalApplication.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        CompositionLocalProvider(
            LocalIndication provides MD3Theme.ripple(),
        ) {
            WIPScreen()
        }
    }
}