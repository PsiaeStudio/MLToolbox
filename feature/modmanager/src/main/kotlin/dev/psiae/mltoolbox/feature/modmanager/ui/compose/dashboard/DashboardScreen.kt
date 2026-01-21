package dev.psiae.mltoolbox.feature.modmanager.ui.compose.dashboard

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.awtClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.core.utils.ensureSuffix
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.fs.AccessDeniedException
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toFsPath
import dev.psiae.mltoolbox.foundation.fs.path.invariantSeparatorsPathString
import dev.psiae.mltoolbox.foundation.fs.path.startsWith
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.domain.model.ManorLordsGameVersion
import dev.psiae.mltoolbox.shared.ui.compose.SimpleTooltip
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.awt.datatransfer.StringSelection
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.readText
import kotlin.text.appendLine

@Composable
internal fun DashboardScreen(modManagerScreenState: ModManagerScreenState) {
    DashboardScreen(rememberDashboardScreenState(rememberDashboardScreenModel(modManagerScreenState)))
}

@Composable
private fun DashboardScreen(
    state: DashboardScreenState
) {
    val snackbar = remember { SnackbarHostState() }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Material3Theme.colorScheme.surface
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                HeightSpacer(16.dp)
                Box(Modifier.align(Alignment.CenterHorizontally)) {
                    GameDirectoriesCard(state.model, snackbar)
                }
                HeightSpacer(16.dp)
                CopyUe4ssLogToClipboardCard(state, snackbar)
                HeightSpacer(4.dp)
                CopyAppLogToClipboardCard(state, snackbar)
            }
            SnackbarHost(
                modifier = Modifier.align(Alignment.BottomCenter),
                hostState = snackbar,
            )
        }
    }
}



@Composable
private fun GameDirectoriesCard(
    model: DashboardScreenModel,
    snackbar: SnackbarHostState,
) {
    val horizontalScroll = rememberScrollState()
    val horizontalScrollIns = remember { MutableInteractionSource() }
    Surface(
        Modifier
            /*.border(
                width = 1.dp,
                color = Material3Theme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )*/,
        shape = Material3Theme.shapes.medium,
        color = Material3Theme.colorScheme.surfaceContainerLow,
    ) {
        Box(
            Modifier
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                ) {
                    val platformDisplayName = model.platformDisplayName()
                    val versionStr = when (val version = model.selectedGameVersion) {
                        is ManorLordsGameVersion -> {
                            when (version) {
                                is ManorLordsGameVersion.Custom -> {
                                    version.customVersionStr + " (Custom)"
                                }
                                is ManorLordsGameVersion.V_0_8_050 -> {
                                    "v0.8.050 (Early Access)"
                                }
                            }
                        }
                        else -> "Unknown"
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f, false)
                            .padding(8.dp)
                            .horizontalScroll(horizontalScroll)
                            .width(IntrinsicSize.Max),
                    ) {
                        val coroutineScope = rememberCoroutineScope()
                        val editGameContextField = {
                            /*coroutineScope.launch {
                                snackbar.currentSnackbarData?.dismiss()
                                snackbar.showSnackbar(message = "Restart the app to edit", withDismissAction = true)
                            }
                            Unit*/
                            model.editGameContext()
                        }
                        SelectedPlatformFieldEditable(model.platformDisplayName(), editGameContextField)
                        SelectedGameVersionFieldEditable(versionStr, editGameContextField)
                        HeightSpacer(10.dp)
                        SelectedInstallFolderFieldEditable(model.selectedInstallFolder, editGameContextField)
                        HeightSpacer(4.dp)
                        SelectedRootFolderFieldEditable(model.selectedRootFolder, editGameContextField)
                        HeightSpacer(4.dp)
                        SelectedGameLauncherExeEditable(model.selectedGameLauncherExe, editGameContextField)
                        HeightSpacer(4.dp)
                        SelectedGameBinaryExeEditable(model.selectedGameBinaryExe, editGameContextField)
                        HeightSpacer(4.dp)
                        SelectedGamePaksFolderEditable(model.selectedGamePaksFolder, editGameContextField)
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
                                        appendLine("Game Platform: ${platformDisplayName}")
                                        appendLine("Game Version: ${
                                            versionStr
                                        }")
                                        appendLine("Game Install folder: ${model.selectedInstallFolder?.invariantSeparatorsPathString?.ensureSuffix("/")}")
                                        appendLine("Game Root folder: ${model.selectedInstallFolder?.invariantSeparatorsPathString?.ensureSuffix("/")}")
                                        appendLine("Game Launcher exe: ${model.selectedGameLauncherExe?.invariantSeparatorsPathString}")
                                        appendLine("Game Binary exe: ${model.selectedGameBinaryExe?.invariantSeparatorsPathString}")
                                        appendLine("Game Paks folder: ${model.selectedGamePaksFolder?.invariantSeparatorsPathString?.ensureSuffix("/")}")
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
                        val absOnSurface = if (LocalIsDarkTheme.current) Color.White else Color.Black
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
}


@Composable
private fun SelectedPlatformFieldEditable(
    selectedPlatformName: String,
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
            text = selectedPlatformName,
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
    versionStr: String,
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
        Text(
            text = versionStr,
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
private fun SelectedInstallFolderFieldEditable(
    path: Path?,
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
                text = path?.invariantSeparatorsPathString?.ensureSuffix('/').toString(),
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
                    .clickable(path != null) {
                        path?.let {
                            uriHandler.openUri(path.toJNioPath().toUri().toString())
                            focusManager.clearFocus()
                        }
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
    path: Path?,
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
                text = path?.invariantSeparatorsPathString?.ensureSuffix('/').toString(),
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
                    .clickable(path != null) {
                        path?.let {
                            uriHandler.openUri(path.toJNioPath().toUri().toString())
                            focusManager.clearFocus()
                        }
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
    path: Path?,
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
                text = path?.invariantSeparatorsPathString.toString(),
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
                    .clickable(path != null) {
                        path?.parent?.let {
                            uriHandler.openUri(path.parent!!.toJNioPath().toUri().toString())
                            focusManager.clearFocus()
                        }
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
    path: Path?,
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
                text = path?.invariantSeparatorsPathString.toString(),
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
                    .clickable(path != null) {
                        path?.parent?.let {
                            uriHandler.openUri(path.parent!!.toJNioPath().toUri().toString())
                            focusManager.clearFocus()
                        }
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
    path: Path?,
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
                text = path?.invariantSeparatorsPathString?.ensureSuffix('/').toString(),
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
                    .clickable(path != null) {
                        path?.let {
                            uriHandler.openUri(path.toJNioPath().toUri().toString())
                            focusManager.clearFocus()
                        }
                    }
            ) {
                if (
                    interactionSource.collectIsHoveredAsState().value ||
                    isFocused
                ) {
                    Icon(
                        modifier = Modifier,
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
private fun CopyUe4ssLogToClipboardCard(
    screenState: DashboardScreenState,
    snackbarState: SnackbarHostState
) {
    var canCopy by remember { mutableStateOf(false) }
    var cantCopyReason by remember { mutableStateOf<String?>(null) }

    val clipboard = checkNotNull(LocalClipboard.current.awtClipboard) {
        "no awt clipboard"
    }
    val coroutineScope = rememberCoroutineScope()
    var lastCopyJob by remember {
        mutableStateOf<Job?>(null)
    }

    val content = @Composable {
        FilledTonalButton(
            onClick = {
                lastCopyJob?.cancel()
                if (lastCopyJob?.isActive != true) {
                    lastCopyJob = coroutineScope.launch {
                        val fs = screenState.fs
                        val gameBinaryPath = screenState.gameContext?.paths?.binary ?: return@launch
                        val gameBinaryFolderPath = gameBinaryPath.parent ?: return@launch
                        var text: String? = null
                        runCatching {
                            withContext(screenState.model.context.dispatch.ioDispatcher) {
                                val ue4ssFolderPath = gameBinaryFolderPath / "ue4ss"
                                val logFilePath = ue4ssFolderPath / "UE4SS.log"
                                if (fs.file(logFilePath).exists())
                                    text = logFilePath.toJNioPath().readText()
                            }
                        }.catchOrRethrow { e ->
                            if (e !is CancellationException)
                                Logger.tryLog { e.stackTraceToString() }
                            if (e is IOException) {
                                snackbarState.currentSnackbarData?.dismiss()
                                snackbarState.showSnackbar("Unable to read logs, IO error")
                                return@launch
                            }
                        }
                        if (text != null) {
                            clipboard.setContents(StringSelection(text), null)
                            snackbarState.currentSnackbarData?.dismiss()
                            snackbarState.showSnackbar("Copied to clipboard", withDismissAction = true)
                        }
                    }
                }
            },
            enabled = canCopy
        ) {
            Text(
                "Copy UE4SS log",
                style = Material3Theme.typography.labelMedium,
                color = Material3Theme.colorScheme.onSecondaryContainer.copy(
                    alpha = if (canCopy) 1f else 0.38f
                ),
            )
        }
    }
    if (canCopy) {
        content()
    } else {
        SimpleTooltip(
            text = cantCopyReason?.ifEmpty { null } ?: "Can't copy",
            content = content
        )
    }

    LaunchedEffect(screenState) {
        val fs = screenState.fs
        val gameBinaryPath = screenState.gameContext?.paths?.binary ?: return@LaunchedEffect
        val gameBinaryFolderPath = gameBinaryPath.parent ?: return@LaunchedEffect
        val ue4ssFolderPath = gameBinaryFolderPath / "ue4ss"
        while (currentCoroutineContext().isActive) {
            run { runCatching {
                withContext(screenState.model.context.dispatch.ioDispatcher) {
                    val logFilePath = ue4ssFolderPath / "UE4SS.log"
                    fs.file(logFilePath).let { file ->
                        if (file.exists() && file.followLinks().isRegularFile()) {
                            canCopy = true
                            cantCopyReason = null
                        } else {
                            canCopy = false
                            cantCopyReason = "Log file does not exist"
                        }
                    }
                }
            }.catchOrRethrow { e ->
                if (e is IOException) {
                    canCopy = false
                    cantCopyReason = when (e) {
                        is AccessDeniedException -> "IO error (Access Denied)"
                        else -> "IO error"
                    }
                    return@run
                }
            } }
            delay(1000)
        }
    }
}

@Composable
private fun CopyAppLogToClipboardCard(
    screenState: DashboardScreenState,
    snackbarState: SnackbarHostState
) {
    var canCopy by remember { mutableStateOf(false) }
    var cantCopyReason by remember { mutableStateOf<String?>(null) }

    val clipboard = checkNotNull(LocalClipboard.current.awtClipboard) {
        "no awt clipboard"
    }
    val coroutineScope = rememberCoroutineScope()
    var lastCopyJob by remember {
        mutableStateOf<Job?>(null)
    }

    val content = @Composable {
        FilledTonalButton(
            onClick = {
                lastCopyJob?.cancel()
                if (lastCopyJob?.isActive != true) {
                    lastCopyJob = coroutineScope.launch {

                        fun naturalCompare(s1: String, s2: String): Int {
                            val splitRegex = Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")

                            val segments1 = s1.split(splitRegex)
                            val segments2 = s2.split(splitRegex)

                            val length = minOf(segments1.size, segments2.size)

                            for (i in 0 until length) {
                                val part1 = segments1[i]
                                val part2 = segments2[i]

                                val isNum1 = part1[0].isDigit()
                                val isNum2 = part2[0].isDigit()

                                val result = if (isNum1 && isNum2) {
                                    val num1 = part1.toBigIntegerOrNull() ?: part1.length.compareTo(part2.length).toBigInteger()
                                    val num2 = part2.toBigIntegerOrNull() ?: part2.length.compareTo(part1.length).toBigInteger()
                                    num1.compareTo(num2)
                                } else {
                                    part1.compareTo(part2, ignoreCase = true)
                                }

                                if (result != 0) return result
                            }

                            return segments1.size - segments2.size
                        }

                        val fs = screenState.fs
                        var text: String? = null
                        runCatching {
                            withContext(screenState.model.context.dispatch.ioDispatcher) {
                                val logsFolder = fs.file(MLToolboxApp.logsDir.toFsPath())
                                if (logsFolder.exists() && logsFolder.isDirectory(followLinks = true)) {
                                    fs.list(logsFolder.path).filter {
                                        it.name.startsWith("log.") && it.name.endsWith(".txt")
                                    }.sortedWith { p1, p2 ->
                                        p1.toJNioPath().getLastModifiedTime().compareTo(p2.toJNioPath().getLastModifiedTime())
                                    }.lastOrNull()?.let { logFile ->
                                        text = logFile.toJNioPath().readText()
                                    }
                                }
                            }
                        }.catchOrRethrow { e ->
                            if (e !is CancellationException)
                                Logger.tryLog { e.stackTraceToString() }
                            if (e is IOException) {
                                snackbarState.currentSnackbarData?.dismiss()
                                snackbarState.showSnackbar("Unable to read logs, IO error")
                                return@launch
                            }
                        }
                        if (text != null) {
                            clipboard.setContents(StringSelection(text), null)
                            snackbarState.currentSnackbarData?.dismiss()
                            snackbarState.showSnackbar("Copied to clipboard", withDismissAction = true)
                        }
                    }
                }
            },
            enabled = canCopy
        ) {
            Text(
                "Copy App log",
                style = Material3Theme.typography.labelMedium,
                color = Material3Theme.colorScheme.onSecondaryContainer.copy(
                    alpha = if (canCopy) 1f else 0.38f
                ),
            )
        }
    }
    if (canCopy) {
        content()
    } else {
        SimpleTooltip(
            text = cantCopyReason?.ifEmpty { null } ?: "Can't copy",
            content = content
        )
    }

    LaunchedEffect(screenState) {
        val fs = screenState.fs
        while (currentCoroutineContext().isActive) {
            run { runCatching {
                withContext(screenState.model.context.dispatch.ioDispatcher) {
                    val logsFolder = fs.file(MLToolboxApp.logsDir.toFsPath())
                    if (
                        logsFolder.exists() &&
                        logsFolder.isDirectory(followLinks = true) &&
                        fs.list(logsFolder.path).any {
                            it.name.startsWith("log.") && it.name.endsWith(".txt")
                        }
                    ) {
                        canCopy = true
                        cantCopyReason = null
                    } else {
                        canCopy = false
                        cantCopyReason = "Log file does not exist"
                    }
                }
            }.catchOrRethrow { e ->
                if (e is IOException) {
                    canCopy = false
                    cantCopyReason = when (e) {
                        is AccessDeniedException -> "IO error (Access Denied)"
                        else -> "IO error"
                    }
                    return@run
                }
            } }
            delay(1000)
        }
    }
}