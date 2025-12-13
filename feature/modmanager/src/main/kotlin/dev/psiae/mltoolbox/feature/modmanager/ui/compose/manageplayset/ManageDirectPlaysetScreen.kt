package dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.inert
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf

@Composable
fun ManageDirectPlaysetScreen(
    managePlaysetScreenModel: ManagePlaysetScreenModel
) {
    val screenModel = rememberManageDirectPlaysetScreenModel(managePlaysetScreenModel)
    val screenState = rememberManageDirectPlaysetScreenState(screenModel)
    ManageDirectPlaysetScreen(screenState)
}

@Composable
private fun ManageDirectPlaysetScreen(
    screenState: ManageDirectPlaysetScreenState
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.thenIf(screenState.navigator.stack.isNotEmpty()) {
                Modifier.inert()
            }
        ) {
            MainUI(screenState)
        }
        screenState.navigator.stack.forEachIndexed { i, e ->
            key(e.id) {
                Box(
                    modifier = Modifier.thenIf(i < screenState.navigator.stack.lastIndex) {
                        Modifier.inert()
                    }
                ) {
                }
            }
        }
    }
}

@Composable
private fun MainUI(
    screenState: ManageDirectPlaysetScreenState
) {
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
        ) {
            HeightSpacer(16.dp)
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                PlaysetListSection(screenState)
            }
        }
    }
}

@Composable
private fun PlaysetListSection(
    screenState: ManageDirectPlaysetScreenState
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Material3Theme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(8.dp)
    ) {
        val gameContext = screenState.model.managePlaysetScreenModel.modManagerScreenModel.gameContext
        if (gameContext != null) {
            PlaysetListUi(
                rememberPlaysetListUiState(
                    screenState.model.context.dispatch,
                    gameContext
                )
            )
        }
    }
}