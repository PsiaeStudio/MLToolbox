package dev.psiae.mltoolbox.feature.modmanager.ui.compose.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenModel
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf
import dev.psiae.mltoolbox.foundation.ui.compose.visibilityGone
import dev.psiae.mltoolbox.shared.ui.compose.SimpleTooltip
import kotlinx.coroutines.delay

@Composable
internal fun LauncherScreen(modManagerScreenModel: ModManagerScreenState) {
    LauncherScreen(rememberLauncherScreenModel(modManagerScreenModel))
}

@Composable
private fun LauncherScreen(
    state: LauncherScreenModel
) {
    val snackbar = remember { SnackbarHostState() }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Material3Theme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                LaunchPanel(state, snackbar)
            }
            SnackbarHost(
                modifier = Modifier.align(Alignment.BottomCenter),
                hostState = snackbar,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LaunchPanel(
    screenModel: LauncherScreenModel,
    snackbar: SnackbarHostState,
) {
    Surface(
        Modifier
            /*.border(
                width = 1.dp,
                color = Material3Theme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )*/,
        color = Material3Theme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        var showLoading by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(36.dp)
                .thenIf(showLoading) {
                    Modifier.visibilityGone()
                }
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .defaultMinSize(minWidth = 120.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Material3Theme.colorScheme.secondaryContainer)
                        .clickable { screenModel.userInputLaunchGame() }
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(50))
                            .size((18+4).dp)
                            .clip(RoundedCornerShape(50)),
                        painter = painterResource("drawable/1.ico"),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    WidthSpacer(8.dp)
                    Text(
                        "Launch Modded Game",
                        style = Material3Theme.typography.labelLarge.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = Material3Theme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                }
                WidthSpacer(24.dp)
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .defaultMinSize(minWidth = 120.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Material3Theme.colorScheme.secondaryContainer)
                        .clickable { screenModel.userInputLaunchVanillaGame() }
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val enabled = true
                    Icon(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(50))
                            .size((18+4).dp)
                            .clip(RoundedCornerShape(50)),
                        painter = painterResource("drawable/1.ico"),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    WidthSpacer(8.dp)
                    Text(
                        "Launch Vanilla Game",
                        style = Material3Theme.typography.labelLarge.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = if (enabled) Material3Theme.colorScheme.onSecondaryContainer else Material3Theme.colorScheme.onSurface.copy(alpha = 0.38f),
                        maxLines = 1
                    )
                    WidthSpacer(12.dp)
                    SimpleTooltip(
                        "all mod will be uninstalled before launching"
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(19.dp)
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(50))
                                    .background(Material3Theme.colorScheme.onError)
                            )
                            Icon(
                                modifier = Modifier
                                    .size((20 + 4).dp)
                                    .align(Alignment.Center),
                                painter = painterResource("drawable/icon_warning_exclamation_mark_filled_24px.png"),
                                contentDescription = null,
                                tint = Material3Theme.colorScheme.error
                            )
                        }
                    }
                }
            }
            if (screenModel.launchingErr) {
                HeightSpacer(36.dp)
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 16.dp),
                    text = screenModel.launchingErrMsg,
                    color = Material3Theme.colorScheme.error,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
        if (screenModel.isLaunching) {
            LaunchedEffect(Unit) {
                delay(200)
                showLoading = true
            }
            if (showLoading) {
                Column(
                    Modifier
                        .defaultMinSize(450.dp)
                        .padding(36.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                        ContainedLoadingIndicator(
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    HeightSpacer(30.dp)
                    Text(
                        modifier = Modifier,
                        text = screenModel.launchingStatusMsg,
                        style = Material3Theme.typography.titleMedium,
                        color = Material3Theme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        } else {
            showLoading = false
        }
    }

}