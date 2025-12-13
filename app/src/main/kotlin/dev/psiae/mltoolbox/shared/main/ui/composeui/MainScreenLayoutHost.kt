package dev.psiae.mltoolbox.shared.main.ui.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import dev.psiae.mltoolbox.shared.ui.compose.StableList
import dev.psiae.mltoolbox.shared.ui.compose.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf
import dev.psiae.mltoolbox.foundation.ui.compose.visibilityInvisible

@Composable
fun MainScreenLayoutScreenHost(
    destinations: StableList<MainDrawerDestination>
) {
    if (destinations.isEmpty()) {
        HostNoDestinationSelected()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .background(remember { Color(29, 24, 34) })
                .defaultSurfaceGestureModifiers()
        ) {
            destinations.fastForEachIndexed { i, destination ->
                key(destination.id) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(remember { Color(29, 24, 34) })
                            .defaultSurfaceGestureModifiers()
                            .thenIf(i != destinations.lastIndex) {
                                Modifier.visibilityInvisible()
                            }
                    ) {
                        destination.content()
                    }
                }
            }
        }
    }
}