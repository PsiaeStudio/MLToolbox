package dev.psiae.mltoolbox.feature.setting.ui.compose.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import dev.psiae.mltoolbox.foundation.ui.compose.inert
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf

@Composable
fun MainSettingScreen(
    state: MainSettingScreenState
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.thenIf(state.navigator.stack.isNotEmpty()) {
                Modifier.inert()
            }
        ) {
            MainUI(state)
        }
        state.navigator.stack.forEachIndexed { i, e ->
            key(e.id) {
                Box(
                    modifier = Modifier.thenIf(i < state.navigator.stack.lastIndex) {
                        Modifier.inert()
                    }
                ) {
                }
            }
        }
    }
}

@Composable
private fun MainUI(screenState: MainSettingScreenState) {

}