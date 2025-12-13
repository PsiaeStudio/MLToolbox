package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.runtime.Composable

import androidx.compose.runtime.RememberObserver
import dev.psiae.mltoolbox.foundation.ui.UiState

/**
 * [Composable] state holder
 */
abstract class ComposeUiState(
) : UiState(), RememberObserver {

    override fun onAbandoned() {
    }

    override fun onForgotten() {
    }

    override fun onRemembered() {
    }
}