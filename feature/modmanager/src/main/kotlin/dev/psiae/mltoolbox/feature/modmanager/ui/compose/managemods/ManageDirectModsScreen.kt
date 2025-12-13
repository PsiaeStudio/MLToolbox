package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallModScreen
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.inert
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf

@Composable
fun ManageDirectModsScreen(
    manageModsScreenModel: ManageModsScreenModel
) {
    val screenModel = rememberManageDirectModsScreenModel(manageModsScreenModel)
    val screenState = rememberManageDirectModsScreenState(screenModel)
    ManageDirectModsScreen(screenState)
}

@Composable
private fun ManageDirectModsScreen(
    screenState: ManageDirectModsScreenState
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
                    when (e.screen) {
                        InstallModScreen -> InstallModScreen(screenState)
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallModScreen(
    screenState: ManageDirectModsScreenState
) {
    val gameContext = screenState.model.manageModsScreenModel.modManagerScreenModel.gameContext
    if (gameContext != null) {
        val state = rememberInstallModScreenState(
            goBack = screenState::contentGoBack,
            gameContext = gameContext
        ) {
            setDirectInstall()
        }
        InstallModScreen(state)
    }
}

@Composable
private fun MainUI(
    screenState: ManageDirectModsScreenState
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
                PrimarySection(screenState)
            }
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ModListSection(screenState)
            }
        }
    }
}

@Composable
private fun PrimarySection(
    screenState: ManageDirectModsScreenState
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Material3Theme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = screenState::installMod)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f, false),
                    text = "Install mod",
                    style = Material3Theme.typography.labelLarge,
                    color = Material3Theme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ModListSection(
    screenState: ManageDirectModsScreenState
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Material3Theme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(8.dp)
    ) {
        val gameContext = screenState.model.manageModsScreenModel.modManagerScreenModel.gameContext
        if (gameContext != null) {
            ModListUi(
                rememberModListUiState(
                    screenState.model.context.dispatch,
                    gameContext
                )
            )
        }
    }
}