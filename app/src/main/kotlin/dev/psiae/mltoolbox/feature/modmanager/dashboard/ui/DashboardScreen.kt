package dev.psiae.mltoolbox.feature.modmanager.dashboard.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.ModManagerScreenState
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.core.java.jPath
import dev.psiae.mltoolbox.core.java.toNioPath
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.shared.ui.compose.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.compose.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import dev.psiae.mltoolbox.core.utils.ensureSuffix
import kotlinx.coroutines.launch
import kotlin.io.path.invariantSeparatorsPathString

@Composable
fun DashboardScreen(modManagerScreenState: ModManagerScreenState) {
    DashboardScreen(rememberDashboardScreenState(modManagerScreenState))
}

@Composable
private fun DashboardScreen(
    state: DashboardScreenState
) {
    val snackbar = remember { SnackbarHostState() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HeightSpacer(16.dp)
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                GameDirectoriesPanel(state, snackbar)
            }
        }
        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            hostState = snackbar,
        )
    }
}



@Composable
private fun GameDirectoriesPanel(
    state: DashboardScreenState,
    snackbar: SnackbarHostState,
) {
    val horizontalScroll = rememberScrollState()
    val horizontalScrollIns = remember { MutableInteractionSource() }
    Box(
        Modifier
            /*.border(
                width = 1.dp,
                color = Material3Theme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )*/
            .clip(RoundedCornerShape(12.dp))
            .background(Material3Theme.colorScheme.surfaceContainerLow)
            .padding(12.dp)
            .defaultSurfaceGestureModifiers()
    ) {
        Column {
            Row(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, false)
                        .padding(8.dp)
                        .horizontalScroll(horizontalScroll)
                        .width(IntrinsicSize.Max),
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    val reopenTheAppToEditFun = {
                        coroutineScope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            snackbar.showSnackbar(message = "Restart the app to edit", withDismissAction = true)
                        }
                        Unit
                    }
                    SelectedPlatformFieldEditable(state.selectedPlatform, reopenTheAppToEditFun)
                    SelectedGameVersionFieldEditable(state.selectedGameVersion, state.selectedGameVersionCustom, reopenTheAppToEditFun)
                    HeightSpacer(10.dp)
                    SelectedInstallFolderFieldEditable(state.selectedInstallFolder, reopenTheAppToEditFun)
                    HeightSpacer(4.dp)
                    SelectedRootFolderFieldEditable(state.selectedRootFolder, reopenTheAppToEditFun)
                    HeightSpacer(4.dp)
                    SelectedGameLauncherExeEditable(state.selectedGameLauncherExe, reopenTheAppToEditFun)
                    HeightSpacer(4.dp)
                    SelectedGameBinaryExeEditable(state.selectedGameBinaryExe, reopenTheAppToEditFun)
                    HeightSpacer(4.dp)
                    SelectedGamePaksFolderEditable(state.selectedGamePaksFolder, reopenTheAppToEditFun)
                }

                val clipboardManager = LocalClipboardManager.current
                val coroutineScope = rememberCoroutineScope()
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Top)
                        .clickable {
                            clipboardManager.setText(
                                buildAnnotatedString {
                                    appendLine("Game Platform: ${state.platformDisplayName()}")
                                    appendLine("Game Version: ${
                                        if (state.selectedGameVersion == "custom")
                                            state.selectedGameVersionCustom + " (custom)"
                                        else
                                            state.selectedGameVersion
                                    }")
                                    appendLine("Game Install folder: ${state.selectedInstallFolder.toNioPath().invariantSeparatorsPathString.ensureSuffix("/")}")
                                    appendLine("Game Root folder: ${state.selectedInstallFolder.toNioPath().invariantSeparatorsPathString.ensureSuffix("/")}")
                                    appendLine("Game Launcher exe: ${state.selectedGameLauncherExe.toNioPath().invariantSeparatorsPathString.ensureSuffix("/")}")
                                    appendLine("Game Binary exe: ${state.selectedGameBinaryExe.toNioPath().invariantSeparatorsPathString.ensureSuffix("/")}")
                                    appendLine("Game Paks folder: ${state.selectedGamePaksFolder.toNioPath().invariantSeparatorsPathString.ensureSuffix("/")}")
                                }
                            )
                            coroutineScope.launch {
                                snackbar.currentSnackbarData?.dismiss()
                                snackbar.showSnackbar(message = "Copied to clipboard", withDismissAction = true)
                            }
                        }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                        painter = painterResource("drawable/icon_copy_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.primary
                    )
                }
            }

            HorizontalScrollbar(
                modifier = Modifier
                    .align(Alignment.Start)
                    .width(
                        with(LocalDensity.current) {
                            remember(this) {
                                derivedStateOf { horizontalScroll.viewportSize.toDp() }
                            }.value
                        }
                    )
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .then(
                        if (horizontalScroll.canScrollForward or horizontalScroll.canScrollBackward)
                            Modifier.background(Color.White.copy(alpha = 0.06f))
                        else Modifier
                    ),
                adapter = rememberScrollbarAdapter(horizontalScroll),
                style = run {
                    val absOnSurface = MD3Theme.currentLocalAbsoluteOnSurfaceColor()
                    defaultScrollbarStyle().copy(
                        unhoverColor = absOnSurface.copy(alpha = 0.25f),
                        hoverColor = absOnSurface.copy(alpha = 0.50f),
                        thickness = 4.dp
                    )
                },
                interactionSource = horizontalScrollIns
            )
        }
    }
}


@Composable
private fun SelectedPlatformFieldEditable(
    selectedPlatform: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .heightIn(min = 24.dp)
            .hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Game Platform: ",
            color = Material3Theme.colorScheme.onSurface,
            style = Material3Theme.typography.bodyLarge
        )
        Text(
            text = when (selectedPlatform) {
                "steam" -> "Steam"
                "xbox_pc_gamepass" -> "Xbox PC Game Pass"
                "epic_games_store" -> "Epic Games Store"
                "gog_com" -> "GOG.com"
                else -> "Unknown"
            },
            color = Material3Theme.colorScheme.onSurface,
            style = Material3Theme.typography.titleMedium
        )

        WidthSpacer(4.dp)

        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        edit()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
@Composable
private fun SelectedGameVersionFieldEditable(
    version: String,
    versionCustom: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .heightIn(min = 24.dp)
            .hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Game Version: ",
            color = Material3Theme.colorScheme.onSurface,
            style = Material3Theme.typography.bodyLarge
        )
        if (version == "custom") {
            Text(
                text = versionCustom,
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.titleMedium
            )
            WidthSpacer(4.dp)
            Text(
                text = "(Custom)",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodyMedium
            )
        } else {
            Text(
                text = version,
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.titleMedium
            )
        }

        WidthSpacer(4.dp)

        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        edit()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedInstallFolderFieldEditable(
    path: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Install folder: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = jPath(path).invariantSeparatorsPathString.ensureSuffix('/'),
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        WidthSpacer(4.dp)


        val focusManager = LocalFocusManager.current
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        edit()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.onSurface,
                    )
                }
            }
        }
        WidthSpacer(4.dp)
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        uriHandler.openUri(jFile(path).toURI().toString())
                        focusManager.clearFocus()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        modifier = Modifier
                           ,
                        painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                        tint = Material3Theme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedRootFolderFieldEditable(
    path: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Root folder: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = jPath(path).invariantSeparatorsPathString.ensureSuffix('/'),
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        WidthSpacer(4.dp)
        val focusManager = LocalFocusManager.current
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        edit()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.onSurface,
                    )
                }
            }
        }
        WidthSpacer(4.dp)
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        uriHandler.openUri(jFile(path).toURI().toString())
                        focusManager.clearFocus()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        modifier = Modifier
                        ,
                        painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                        tint = Material3Theme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedGameLauncherExeEditable(
    path: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Launcher exe: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = jPath(path).invariantSeparatorsPathString,
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        WidthSpacer(4.dp)
        val focusManager = LocalFocusManager.current
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        edit()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.onSurface,
                    )
                }
            }
        }
        WidthSpacer(4.dp)
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        uriHandler.openUri(jFile(path).parentFile.toURI().toString())
                        focusManager.clearFocus()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        modifier = Modifier
                        ,
                        painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                        tint = Material3Theme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
@Composable
private fun SelectedGameBinaryExeEditable(
    path: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Binary exe: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = jPath(path).invariantSeparatorsPathString,
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        WidthSpacer(4.dp)
        val focusManager = LocalFocusManager.current
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        edit()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.onSurface,
                    )
                }
            }
        }
        WidthSpacer(4.dp)
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        uriHandler.openUri(jFile(path).parentFile.toURI().toString())
                        focusManager.clearFocus()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        modifier = Modifier
                        ,
                        painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                        tint = Material3Theme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
@Composable
private fun SelectedGamePaksFolderEditable(
    path: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Paks folder: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = jPath(path).invariantSeparatorsPathString.ensureSuffix("/"),
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        WidthSpacer(4.dp)
        val focusManager = LocalFocusManager.current
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        edit()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.onSurface,
                    )
                }
            }
        }
        WidthSpacer(4.dp)
        Row(
            modifier = Modifier
        ) {
            var isFocused by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onFocusChanged {
                        isFocused = it.hasFocus
                    }
                    .focusGroup()
                    .clickable {
                        uriHandler.openUri(jFile(path).toURI().toString())
                        focusManager.clearFocus()
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        modifier = Modifier
                        ,
                        painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                        tint = Material3Theme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
        }
    }
}