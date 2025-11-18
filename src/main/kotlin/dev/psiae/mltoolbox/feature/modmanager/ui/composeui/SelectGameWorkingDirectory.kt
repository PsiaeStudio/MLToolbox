package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.AwtWindow
import dev.psiae.mltoolbox.shared.ui.composeui.HeightSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.LocalAwtWindow
import dev.psiae.mltoolbox.shared.ui.composeui.WidthSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.collapsible
import dev.psiae.mltoolbox.shared.ui.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.java.jPath
import dev.psiae.mltoolbox.shared.platform.content.filepicker.JnaFileChooserWindowHost
import dev.psiae.mltoolbox.shared.platform.content.filepicker.win32.JnaFileChooser
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.surfaceColorAtElevation
import dev.psiae.mltoolbox.shared.ui.md3.MD3Spec
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import dev.psiae.mltoolbox.shared.ui.md3.incrementsDp
import dev.psiae.mltoolbox.shared.ui.md3.padding
import dev.psiae.mltoolbox.shared.utils.ensureSuffix
import dev.psiae.mltoolbox.shared.utils.orNullString
import dev.psiae.mltoolbox.shared.utils.runtimeError
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import java.io.File
import javax.swing.SwingUtilities
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString

typealias jProcessHandle = ProcessHandle

@Composable
fun SelectGameWorkingDirectoryScreen(
    modManagerScreenState: ModManagerScreenState
) {
    val state = remember(modManagerScreenState) {
        SelectGameWorkingDirectoryState(modManagerScreenState)
    }
    if (state.openFolderPicker) {
        GameFolderPicker(state)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        val scrollState = rememberScrollState()
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center
            ) {
                HeightSpacer(16.dp)
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .sizeIn(maxWidth = 1400.dp, maxHeight = 1400.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults
                        .cardColors(Material3Theme.colorScheme.surfaceContainer, contentColor = Material3Theme.colorScheme.onSurface)
                ) {
                    Column(
                        Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        if (!state.alreadySelectedPlatform) {
                            SelectGamePlatform(state)
                        } else if (!state.alreadySelectedGameFolder) {
                            SelectGameInstallFolder(state)
                        } else if (!state.alreadySelectedGameDirs) {
                            SelectGameDirs(state)
                        } else if (!state.alreadySelectedGameVersion) {
                            SelectGameVersion(state)
                        }

                        HeightSpacer(28.dp)
                        Row {
                            if (!state.alreadySelectedPlatform || !state.alreadySelectedGameFolder || !state.alreadySelectedGameDirs || !state.alreadySelectedGameVersion)
                                RenderPageIndicator(state)
                            WidthSpacer(8.dp)
                        }
                    }
                }
                HeightSpacer(16.dp)
            }
            VerticalScrollbar(
                modifier = Modifier
                    .height(
                        with(LocalDensity.current) {
                            scrollState.viewportSize.toDp()
                        }
                    )
                    .padding(start = 0.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                    .then(
                        if (scrollState.maxValue > 0)
                            Modifier.background(Color.White.copy(alpha = 0.06f))
                        else Modifier
                    ),
                adapter = rememberScrollbarAdapter(scrollState),
                style = run {
                    val absOnSurface = MD3Theme.currentLocalAbsoluteOnSurfaceColor()
                    defaultScrollbarStyle().copy(
                        unhoverColor = absOnSurface.copy(alpha = 0.25f),
                        hoverColor = absOnSurface.copy(alpha = 0.50f),
                        thickness = 4.dp
                    )
                }
            )
        }
    }
}

@Composable
fun ChangeGameWorkingDirectory(modManagerComposeState: ModManagerScreenState) {
    val state = remember(modManagerComposeState) {
        SelectGameWorkingDirectoryState(modManagerComposeState)
            .apply { userInputOpenGameDirPicker() }
    }
    if (state.openFolderPicker) {
        GameFolderPicker(state)
    }
}

@Composable
private fun GameFolderPicker(state: SelectGameWorkingDirectoryState) {
    val window = LocalAwtWindow.current
    LaunchedEffect(Unit) {
        val pick = FileKit.pickDirectory(
            title = "Select game folder (Manor Lords)",
            platformSettings = FileKitPlatformSettings(parentWindow = window),
            initialDirectory = state.pickedGameFolder?.absolutePath
        )
        state.filePick(pick?.file)
    }
}

@Composable
private fun NativeFilePickerDialog(
    title: String,
    initialDir: String?,
    initialFileName: String,
    onCloseRequest: (File?) -> Unit,
    filter: () -> List<Pair<String, Array<String>>>? = { null },
    mode: JnaFileChooser.Mode = JnaFileChooser.Mode.Files,
    multiSelect: Boolean = true,
    key: Any = ""
) {
    key(key) {
        val window = LocalAwtWindow.current
        AwtWindow(
            visible = false,
            create = {
                JnaFileChooserWindowHost(window, title, initialDir, initialFileName, filter, mode, multiSelect)
                    .apply {
                        openAndInvokeOnCompletion { result ->
                            SwingUtilities.invokeLater {
                                onCloseRequest(result.getOrThrow())
                            }
                        }
                    }
            },
            dispose = JnaFileChooserWindowHost::dispose
        )
    }
}

@Composable
private fun SelectGamePlatform(
    state: SelectGameWorkingDirectoryState
) {
    Column {
        Text(
            text = "Select game platform",
            color = Material3Theme.colorScheme.onSurface,
            style = Material3Theme.typography.headlineSmall
        )
        HeightSpacer(16.dp)

        val focusRequesters = remember {
            mutableListOf<FocusRequester>()
        }
        val selections = remember {
            mutableListOf<String>(
                "steam", "xbox_pc_gamepass", "gog_com", "epic_games_store"
            )
        }
        val selectedPlatformIndex by remember {
            derivedStateOf { selections.indexOf(state.selectedPlatform) }
        }
        var showStateLayer by remember {
            mutableStateOf(false)
        }
        val focusRequesterSteam = remember {
            FocusRequester()
                .also { focusRequesters.add(it) }
        }.also {
            LaunchedEffect(Unit) {
                it.requestFocus()
            }
        }
        val focusRequesterXbox = remember {
            FocusRequester()
                .also { focusRequesters.add(it) }
        }
        val focusRequesterGOG = remember {
            FocusRequester()
                .also { focusRequesters.add(it) }
        }
        val focusRequesterEpic = remember {
            FocusRequester()
                .also { focusRequesters.add(it) }
        }
        var focusRequesterIndex by remember {
            mutableStateOf(-1)
        }
        Column(
            modifier = Modifier
                .widthIn(min = 450.dp, max = 600.dp)
                .width(IntrinsicSize.Max)
                .onKeyEvent { event ->
                    Snapshot.withoutReadObservation {
                        if (event.type == KeyEventType.KeyDown) {
                            when(event.key) {
                                Key.DirectionDown -> {
                                    val nextIndex = focusRequesterIndex.plus(1).coerceAtMost(selections.lastIndex)
                                    if (nextIndex >= 0) {
                                        focusRequesters[nextIndex].requestFocus()
                                    }
                                    true
                                }
                                Key.DirectionUp -> {
                                    val nextIndex = focusRequesterIndex.plus(-1).coerceAtMost(selections.lastIndex)
                                    if (nextIndex >= 0) {
                                        focusRequesters[nextIndex].requestFocus()
                                    }
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    }
                }.onFocusChanged {
                    if (!it.hasFocus)
                        focusRequesterIndex = -1
                }.focusGroup()
        ) {
            val isSteamSelected = state.selectedPlatform == "steam"
            Row(
                modifier = Modifier
                    .then(if (isSteamSelected) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                    .focusRequester(focusRequesterSteam)
                    .onFocusChanged { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterSteam) }
                    .then(if (focusRequesterIndex == focusRequesters.indexOf(focusRequesterSteam)) Modifier.background(
                        Material3Theme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)) else Modifier)
                    .clickable(/*!isSteamSelected*/true) {state.selectedPlatform = "steam";state.commitSelectPlatform()}
                    .padding(horizontal = 8.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.height(42.dp)) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        painter = painterResource("drawable/icon_steam_logo_convert_32px.png"),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
                WidthSpacer(12.dp)
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            Material3Theme.typography.bodyLarge.toSpanStyle()
                                .copy(fontWeight = FontWeight.Medium)
                        ) {
                            append("Steam".replace(' ', '\u00A0'))
                        }
                    },
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                WidthSpacer(24.dp)
                Spacer(Modifier.weight(1f))
                /*RadioButton(
                    selected = selectedPlatform == "steam",
                    onClick = { selectedPlatform = "steam" },
                    modifier = Modifier.size(24.dp),
                    enabled = true,
                    colors = RadioButtonDefaults.colors()
                )*/
            }
            val isXboxSelected = state.selectedPlatform == "xbox_pc_gamepass"
            Row(
                modifier = Modifier
                    .then(if (isXboxSelected) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                    .focusRequester(focusRequesterXbox)
                    .onFocusChanged { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterXbox) }
                    .then(if (focusRequesterIndex == focusRequesters.indexOf(focusRequesterXbox)) Modifier.background(
                        Material3Theme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)) else Modifier)
                    .clickable(/*!isXboxSelected*/true) {state.selectedPlatform = "xbox_pc_gamepass";state.commitSelectPlatform()}
                    .padding(horizontal = 8.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.height(42.dp)) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        painter = painterResource("drawable/icon_xbox_logo_convert_32px.png"),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
                WidthSpacer(12.dp)
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            Material3Theme.typography.bodyLarge.toSpanStyle()
                                .copy(fontWeight = FontWeight.Medium)
                        ) {
                            append("Xbox PC Game Pass".replace(' ', '\u00A0'))
                        }
                    },
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                WidthSpacer(24.dp)
                Spacer(Modifier.weight(1f))
                /*RadioButton(
                    selected = selectedPlatform == "xbox_pc_gamepass",
                    onClick = { selectedPlatform = "xbox_pc_gamepass" },
                    modifier = Modifier.size(24.dp),
                    enabled = true,
                    colors = RadioButtonDefaults.colors()
                )*/
            }


            val isGOGSelected = state.selectedPlatform == "gog_com"
            Row(
                modifier = Modifier
                    .then(if (isGOGSelected) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                    .focusRequester(focusRequesterGOG)
                    .onFocusChanged { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterGOG) }
                    .then(if (focusRequesterIndex == focusRequesters.indexOf(focusRequesterGOG)) Modifier.background(
                        Material3Theme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)) else Modifier)
                    .clickable(/*!isGOGSelected*/true) {state.selectedPlatform = "gog_com";state.commitSelectPlatform()}
                    .padding(horizontal = 8.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource("drawable/GOG.com_logo_44x42.png"),
                        contentDescription = null,
                        tint = MD3Theme.currentLocalAbsoluteOnSurfaceColor()
                    )
                }
                WidthSpacer(12.dp)
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            Material3Theme.typography.bodyLarge.toSpanStyle()
                                .copy(fontWeight = FontWeight.Medium)
                        ) {
                            append("GOG.com".replace(' ', '\u00A0'))
                        }
                    },
                    color = if (isGOGSelected) Material3Theme.colorScheme.onSecondaryContainer else Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                WidthSpacer(24.dp)
                Spacer(Modifier.weight(1f))
                /*RadioButton(
                    selected = selectedPlatform == "gog_com",
                    onClick = { selectedPlatform = "gog_com" },
                    modifier = Modifier.size(24.dp),
                    enabled = true,
                    colors = RadioButtonDefaults.colors()
                )*/
            }

            val isEpicSelected = state.selectedPlatform == "epic_games_store"
            Row(
                modifier = Modifier
                    .then(if (isEpicSelected) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                    .focusRequester(focusRequesterEpic)
                    .onFocusChanged { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterEpic) }
                    .then(if (focusRequesterIndex == focusRequesters.indexOf(focusRequesterEpic)) Modifier.background(
                        Material3Theme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)) else Modifier)
                    .clickable(/*!isEpicSelected*/true) {state.selectedPlatform = "epic_games_store";state.commitSelectPlatform()}
                    .padding(horizontal = 10.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.height(42.dp)) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        painter = if (LocalIsDarkTheme.current)
                            painterResource("drawable/EGS-Logotype-2023-Horizontal-White_106x38.png")
                        else
                            painterResource("drawable/EGS-Logotype-2023-Horizontal-Black_106x38.png")
                        ,
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
                WidthSpacer(12.dp)
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            Material3Theme.typography.bodyLarge.toSpanStyle()
                                .copy(fontWeight = FontWeight.Medium)
                        ) {
                            append("Epic Games Store".replace(' ', '\u00A0'))
                        }
                    },
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                WidthSpacer(24.dp)
                Spacer(Modifier.weight(1f))
                /*RadioButton(
                    selected = selectedPlatform == "epic_games_store",
                    onClick = { selectedPlatform = "epic_games_store" },
                    modifier = Modifier.size(24.dp),
                    enabled = true,
                    colors = RadioButtonDefaults.colors()
                )*/
            }

        }
        /*HeightSpacer(28.dp)
        Box(
            modifier = Modifier.align(Alignment.End)
                .defaultMinSize(minHeight = 40.dp, minWidth = 120.dp)
                .clip(RoundedCornerShape(50))
                .background(Material3Theme.colorScheme.primary.copy(alpha = if (state.selectedPlatform.isNotBlank()) 1f else 0.6f))
                .clickable(state.selectedPlatform.isNotBlank()) {
                    state.commitSelectPlatform()
                }
                .padding(vertical = 6.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row {
                Text(
                    text = "Continue",
                    color = Material3Theme.colorScheme.onPrimary.copy(alpha = if (state.selectedPlatform.isNotBlank()) 1f else 0.38f),
                    maxLines = 1,
                    style = Material3Theme.typography.labelLarge
                )
            }
        }*/
    }
}

@Composable
private fun SelectGameInstallFolder(
    state: SelectGameWorkingDirectoryState
) {
    Column {
        Text(
            text = "Select game install folder",
            color = Material3Theme.colorScheme.onSurface,
            style = Material3Theme.typography.headlineSmall
        )
        HeightSpacer(16.dp)
        Column(modifier = Modifier.widthIn(min = 450.dp, max = 600.dp).width(IntrinsicSize.Max)) {

            run {
                val interactionSource = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .heightIn(min = 24.dp)
                        .hoverable(interactionSource),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected platform: ",
                        color = Material3Theme.colorScheme.onSurface,
                        style = Material3Theme.typography.bodyMedium
                    )
                    Text(
                        text = when (state.selectedPlatform) {
                            "steam" -> "Steam"
                            "xbox_pc_gamepass" -> "Xbox PC Game Pass"
                            "epic_games_store" -> "Epic Games Store"
                            "gog_com" -> "GOG.com"
                            else -> "Unknown"
                        },
                        color = Material3Theme.colorScheme.onSurface,
                        style = Material3Theme.typography.labelLarge
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
                                    state.changePlatform()
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
            HeightSpacer(12.dp)
            OutlinedCard(
                modifier = Modifier,
                colors = CardDefaults.cardColors(
                    containerColor = Material3Theme.colorScheme.surfaceContainer,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    HeightSpacer(4.dp)
                    PickFolderWidget(state, showExample = true)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SelectGameDirs(
    state: SelectGameWorkingDirectoryState
) {
    val isLoading by derivedStateOf { state.processingPickedFolder }
    var showLoading by remember {
        mutableStateOf(false)
    }.apply {
        if (!isLoading)
            value = false
    }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(200)
            showLoading = true
        }
    }
    Box(
        modifier = Modifier
    ) {
        Column {
            Text(
                text = "Select game directories",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.headlineSmall
            )
            HeightSpacer(16.dp)
            Column(
                modifier = Modifier
                    .widthIn(min = 360.dp, max = 1200.dp)
                    .width(IntrinsicSize.Max)
                    .collapsible(showLoading)
            ) {
                run {
                    val interactionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier.hoverable(interactionSource),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected platform: ",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (state.selectedPlatform) {
                                "steam" -> "Steam"
                                "xbox_pc_gamepass" -> "Xbox PC Game Pass"
                                "epic_games_store" -> "Epic Games Store"
                                "gog_com" -> "GOG.com"
                                else -> "Unknown"
                            },
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                        state.changePlatform()
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
                HeightSpacer(4.dp)
                run {
                    val interactionSource = remember { MutableInteractionSource() }
                    val path = state.pickedGameFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/')
                    Row(
                        modifier = Modifier.hoverable(interactionSource),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(Modifier.weight(1f, false)) {
                            Text(
                                text = "Selected install folder: ",
                                color = Material3Theme.colorScheme.onSurface,
                                style = Material3Theme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = path.orEmpty(),
                                color = Material3Theme.colorScheme.onSurface,
                                style = Material3Theme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                        state.changeFolder()
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
                HeightSpacer(12.dp)
                var editFieldKey by remember {
                    mutableStateOf("")
                }
                if (editFieldKey.isNotEmpty()) {
                    when (editFieldKey) {
                        "game_root_folder" -> GameRootFolderFieldEdit(state, {editFieldKey = ""})
                        "game_launcher_exe" -> GameLauncherExeFieldEdit(state, {editFieldKey = ""})
                        "game_binary_exe" -> GameBinaryExeFieldEdit(state, {editFieldKey = ""})
                        "game_paks_folder" -> GamePaksFolderFieldEdit(state, {editFieldKey = ""})
                        else -> runtimeError("unknown editField: $editFieldKey")
                    }
                } else {
                    GameDirectoriesBlock(state, {editFieldKey = it})
                }

                HeightSpacer(24.dp)

                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(
                            if (state.hasAllDirectoriesSelected)
                                Material3Theme.colorScheme.primary
                            else
                                Material3Theme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(50)
                        )
                        .clip(RoundedCornerShape(50))
                        .clickable(state.hasAllDirectoriesSelected) {
                            state.userSelectGameDirs()
                        }
                        .height(40.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Continue",
                        color = if (state.hasAllDirectoriesSelected)
                            Material3Theme.colorScheme.onPrimary
                        else
                            Material3Theme.colorScheme.onSurface.copy(alpha = 0.38f),
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isLoading) {
                HeightSpacer(8.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLoading) {
                        ContainedLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameDirectoriesBlock(
    state: SelectGameWorkingDirectoryState,
    editField: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = Material3Theme.colorScheme.surfaceContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            GameRootFolderField(
                state = state,
                edit = {editField("game_root_folder") },
            )
            GameLauncherExeField(
                state = state,
                edit = {editField("game_launcher_exe") },
            )
            GameBinaryExeField(
                state = state,
                edit = {editField("game_binary_exe") },
            )
            GamePaksFolderField(
                state = state,
                edit = {editField("game_paks_folder") },
            )

            if (!state.hasAllDirectoriesSelected) {
                HeightSpacer(16.dp)
                Text(
                    text = "* Fill required directories to continue",
                    color = Material3Theme.colorScheme.error,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                HeightSpacer(3.dp)
                Text(
                    text = "* Help us by submitting unknown file paths",
                    color = Material3Theme.colorScheme.error,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                HeightSpacer(16.dp)
                Text(
                    text = "* Make sure selected directories are correct",
                    color = Material3Theme.colorScheme.onSurfaceVariant,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}



@Composable
private fun GameRootFolderFieldEdit(
    state: SelectGameWorkingDirectoryState,
    done: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.defaultMinSize(minWidth = 140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalIsDarkTheme.current) Material3Theme.colorScheme.surfaceContainerHighest
            else Material3Theme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            val textFieldState = remember {
                TextFieldState(state.gameRootFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/') ?: "")
            }
            val interactionSource = remember { MutableInteractionSource() }
            val validStartsWith by remember {
                derivedStateOf {
                    val text = textFieldState.text
                    !text.isBlank() && jFile(text.toString()).startsWith(state.requirePickedFolder())
                }
            }
            val validIsFolderState = remember(textFieldState.text) {
                mutableStateOf(false)
            }.apply {
                LaunchedEffect(this) {
                    withContext(Dispatchers.IO) {
                        value = jFile(textFieldState.text.toString()).isDirectory
                    }
                }
            }
            val isValid by remember(validIsFolderState) {
                derivedStateOf {
                    validStartsWith and validIsFolderState.value
                }
            }
            OutlinedTextField(
                modifier = Modifier.height(48.dp).sizeIn(minWidth = 600.dp),
                state = textFieldState,
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = "Game Root folder path",
                        color = if (interactionSource.collectIsFocusedAsState().value)
                            Material3Theme.colorScheme.primary
                        else
                            Material3Theme.colorScheme.onSurfaceVariant,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                labelPosition = TextFieldLabelPosition.Attached(),
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                isError = !isValid
            )
            HeightSpacer(4.dp)
            Row {
                WidthSpacer(16.dp)
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Starts with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(state.pickedGameFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/') ?: "")
                            }
                        },
                        color = if (validStartsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Is Folder")
                        },
                        color = if (validIsFolderState.value)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HeightSpacer(16.dp)

            Text(
                text = buildAnnotatedString {
                    append("example: ")
                    withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                        append(
                            when (state.selectedPlatform) {
                                "steam" -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/"
                                "xbox_pc_gamepass" -> "C:/XboxGames/Manor Lords/Content/"
                                "epic_games_store" -> "C:/Program Files/Epic Games/Manor Lords/"
                                "gog_com" -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/"
                                else -> runtimeError("Unknown selected platform: ${state.selectedPlatform}")
                            }.replace(' ', '\u00A0')
                        )
                    }
                },
                color = Material3Theme.colorScheme.onSurfaceVariant,
                style = Material3Theme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HeightSpacer(24.dp)
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    modifier = Modifier.height(40.dp),
                    onClick = done,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Cancel",
                        color = Material3Theme.colorScheme.primary,
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(
                    modifier = Modifier.height(40.dp),
                    enabled = isValid,
                    onClick = {
                        state.userPickGameRootFolder(jFile(textFieldState.text.toString()))
                        done()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Done",
                        color = Material3Theme.colorScheme.primary.copy(alpha = if (isValid) 1f else 0.38f),
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GameLauncherExeFieldEdit(
    state: SelectGameWorkingDirectoryState,
    done: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.defaultMinSize(minWidth = 140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalIsDarkTheme.current) Material3Theme.colorScheme.surfaceContainerHighest
            else Material3Theme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            val textFieldState = remember {
                TextFieldState(state.gameLauncherExeFile?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString ?: "")
            }
            val interactionSource = remember { MutableInteractionSource() }
            val validStartsWith by remember {
                derivedStateOf {
                    val text = textFieldState.text
                    !text.isBlank() && jFile(text.toString()).startsWith(state.requirePickedFolder())
                }
            }
            val validEndsWith by remember {
                derivedStateOf {
                    val text = textFieldState.text
                    val textStr = text.toString()
                    !textStr.isBlank() &&
                            textStr.endsWith(".exe") &&
                            textStr.substringBeforeLast(".exe").takeLastWhile { it != '\\' && it != '/' }.isNotEmpty()
                }
            }
            val validIsFileState = remember(textFieldState.text) {
                mutableStateOf(false)
            }.apply {
                LaunchedEffect(this) {
                    withContext(Dispatchers.IO) {
                        value = jFile(textFieldState.text.toString()).isFile
                    }
                }
            }
            val isValid by remember(validIsFileState) {
                derivedStateOf {
                    validStartsWith and validEndsWith and validIsFileState.value
                }
            }
            OutlinedTextField(
                modifier = Modifier.height(48.dp).sizeIn(minWidth = 600.dp),
                state = textFieldState,
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = "Game Launcher exe path",
                        color = if (interactionSource.collectIsFocusedAsState().value)
                            Material3Theme.colorScheme.primary
                        else
                            Material3Theme.colorScheme.onSurfaceVariant,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                labelPosition = TextFieldLabelPosition.Attached(),
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                isError = !isValid
            )
            HeightSpacer(4.dp)
            Row {
                WidthSpacer(16.dp)
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Starts with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(state.gameRootFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/') ?: "")
                            }
                        },
                        color = if (validStartsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Ends with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(".exe")
                            }
                        },
                        color = if (validEndsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Is file")
                        },
                        color = if (validIsFileState.value)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HeightSpacer(16.dp)

            Text(
                text = buildAnnotatedString {
                    append("example: ")
                    withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                        append(
                            when (state.selectedPlatform) {
                                "steam" -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/ManorLords.exe"
                                "xbox_pc_gamepass" -> "C:/XboxGames/Manor Lords/Content/gamelaunchhelper.exe"
                                "epic_games_store" -> "C:/Program Files/Epic Games/Manor Lords/ManorLords/ManorLords.exe"
                                "gog_com" -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/ManorLords/ManorLords.exe"
                                else -> runtimeError("Unknown selected platform: ${state.selectedPlatform}")
                            }.replace(' ', '\u00A0')
                        )
                    }
                },
                color = Material3Theme.colorScheme.onSurfaceVariant,
                style = Material3Theme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )


            HeightSpacer(16.dp)
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    modifier = Modifier.height(40.dp),
                    onClick = done,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Cancel",
                        color = Material3Theme.colorScheme.primary,
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(
                    modifier = Modifier.height(40.dp),
                    enabled = isValid,
                    onClick = {
                        state.userPickGameLauncherExe(jFile(textFieldState.text.toString()))
                        done()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Done",
                        color = Material3Theme.colorScheme.primary.copy(alpha = if (isValid) 1f else 0.38f),
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GameBinaryExeFieldEdit(
    state: SelectGameWorkingDirectoryState,
    done: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.defaultMinSize(minWidth = 140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalIsDarkTheme.current) Material3Theme.colorScheme.surfaceContainerHighest
            else Material3Theme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            val textFieldState = remember {
                TextFieldState(state.gameBinaryExeFile?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString ?: "")
            }
            val interactionSource = remember { MutableInteractionSource() }
            val validStartsWith by remember {
                derivedStateOf {
                    val text = textFieldState.text
                    !text.isBlank() && jFile(text.toString()).startsWith(state.requirePickedFolder())
                }
            }
            val validEndsWith by remember {
                derivedStateOf {
                    val text = textFieldState.text
                    val textStr = text.toString()
                    !textStr.isBlank() &&
                        textStr.endsWith(".exe") &&
                        textStr.substringBeforeLast(".exe").takeLastWhile { it != '\\' && it != '/' }.isNotEmpty()
                }
            }
            val validIsFileState = remember(textFieldState.text) {
                mutableStateOf(false)
            }.apply {
                LaunchedEffect(this) {
                    withContext(Dispatchers.IO) {
                        value = jFile(textFieldState.text.toString()).isFile
                    }
                }
            }
            val isValid by remember(validIsFileState) {
                derivedStateOf {
                    validStartsWith and validEndsWith and validIsFileState.value
                }
            }
            OutlinedTextField(
                modifier = Modifier.height(48.dp).sizeIn(minWidth = 600.dp),
                state = textFieldState,
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = "Game Binary exe path",
                        color = if (interactionSource.collectIsFocusedAsState().value)
                            Material3Theme.colorScheme.primary
                        else
                            Material3Theme.colorScheme.onSurfaceVariant,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                labelPosition = TextFieldLabelPosition.Attached(),
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                isError = !isValid
            )
            HeightSpacer(4.dp)
            Row {
                WidthSpacer(16.dp)
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Starts with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(state.gameRootFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/') ?: "")
                            }
                        },
                        color = if (validStartsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Ends with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(".exe")
                            }
                        },
                        color = if (validEndsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Is file")
                        },
                        color = if (validIsFileState.value)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HeightSpacer(16.dp)

            Text(
                text = buildAnnotatedString {
                    append("example: ")
                    withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                        append(
                            when (state.selectedPlatform) {
                                "steam" -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/ManorLords/Binaries/Win64/ManorLords-Win64-Shipping.exe"
                                "xbox_pc_gamepass" -> "C:/XboxGames/Manor Lords/Content/ManorLords/Binaries/WinGDK/ManorLords-WinGDK-Shipping.exe"
                                "epic_games_store" -> "C:/Program Files/Epic Games/Manor Lords/ManorLords/Binaries/Win64/ManorLords-Win64-Shipping.exe"
                                "gog_com" -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/ManorLords/Binaries/Win64/ManorLords-Win64-Shipping.exe"
                                else -> runtimeError("Unknown selected platform: ${state.selectedPlatform}")
                            }.replace(' ', '\u00A0')
                        )
                    }
                },
                color = Material3Theme.colorScheme.onSurfaceVariant,
                style = Material3Theme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HeightSpacer(16.dp)
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    modifier = Modifier.height(40.dp),
                    onClick = done,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Cancel",
                        color = Material3Theme.colorScheme.primary,
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(
                    modifier = Modifier.height(40.dp),
                    enabled = isValid,
                    onClick = {
                        state.userPickGameBinaryExe(jFile(textFieldState.text.toString()))
                        done()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Done",
                        color = Material3Theme.colorScheme.primary.copy(alpha = if (isValid) 1f else 0.38f),
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GamePaksFolderFieldEdit(
    state: SelectGameWorkingDirectoryState,
    done: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.defaultMinSize(minWidth = 140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalIsDarkTheme.current) Material3Theme.colorScheme.surfaceContainerHighest
            else Material3Theme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            val textFieldState = remember {
                TextFieldState(state.gamePaksFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/') ?: "")
            }
            val interactionSource = remember { MutableInteractionSource() }
            val validStartsWith by remember {
                derivedStateOf {
                    val text = textFieldState.text
                    !text.isBlank() && jFile(text.toString()).startsWith(state.requirePickedFolder())
                }
            }
            val validIsFolderState = remember(textFieldState.text) {
                mutableStateOf(false)
            }.apply {
                LaunchedEffect(this) {
                    withContext(Dispatchers.IO) {
                        value = jFile(textFieldState.text.toString()).isDirectory
                    }
                }
            }
            val isValid by remember(validIsFolderState) {
                derivedStateOf {
                    validStartsWith and validIsFolderState.value
                }
            }
            OutlinedTextField(
                modifier = Modifier.height(48.dp).sizeIn(minWidth = 600.dp),
                state = textFieldState,
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = "Game Paks folder path",
                        color = if (interactionSource.collectIsFocusedAsState().value)
                            Material3Theme.colorScheme.primary
                        else
                            Material3Theme.colorScheme.onSurfaceVariant,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                labelPosition = TextFieldLabelPosition.Attached(),
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                isError = !isValid
            )
            HeightSpacer(4.dp)
            Row {
                WidthSpacer(16.dp)
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Starts with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(state.gameRootFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/') ?: "")
                            }
                        },
                        color = if (validStartsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Is Folder")
                        },
                        color = if (validIsFolderState.value)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HeightSpacer(16.dp)

            Text(
                text = buildAnnotatedString {
                    append("example: ")
                    withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                        append(
                            when (state.selectedPlatform) {
                                "steam" -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/ManorLords/Content/Paks"
                                "xbox_pc_gamepass" -> "C:/XboxGames/Manor Lords/Content/ManorLords/Content/Paks"
                                "epic_games_store" -> "C:/Program Files/Epic Games/Manor Lords/ManorLords/Content/Paks"
                                "gog_com" -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/ManorLords/Content/Paks"
                                else -> runtimeError("Unknown selected platform: ${state.selectedPlatform}")
                            }.replace(' ', '\u00A0')
                        )
                    }
                },
                color = Material3Theme.colorScheme.onSurfaceVariant,
                style = Material3Theme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HeightSpacer(16.dp)
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    modifier = Modifier.height(40.dp),
                    onClick = done,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Cancel",
                        color = Material3Theme.colorScheme.primary,
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(
                    modifier = Modifier.height(40.dp),
                    enabled = isValid,
                    onClick = {
                        state.userPickGamePaksFolder(jFile(textFieldState.text.toString()))
                        done()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Done",
                        color = Material3Theme.colorScheme.primary.copy(alpha = if (isValid) 1f else 0.38f),
                        style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GameRootFolderField(
    state: SelectGameWorkingDirectoryState,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val path = state.gameRootFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/')
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Root folder: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = path.orEmpty(),
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                    .size(24.dp)
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
private fun GameLauncherExeField(
    state: SelectGameWorkingDirectoryState,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val path = state.gameLauncherExeFile?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Launcher exe: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = path ?: "",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                    .size(24.dp)
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
private fun GameBinaryExeField(
    state: SelectGameWorkingDirectoryState,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val path = state.gameBinaryExeFile?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Binary exe: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = path ?: "",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                    .size(24.dp)
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
private fun GamePaksFolderField(
    state: SelectGameWorkingDirectoryState,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val path = state.gamePaksFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/')
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Game Paks folder: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = path.orEmpty(),
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                    .size(24.dp)
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
private fun PickFolderWidget(
    state: SelectGameWorkingDirectoryState,
    modifier: Modifier = Modifier,
    showExample: Boolean = false,
) {
    Column {
        Box(modifier = modifier
            .align(Alignment.CenterHorizontally)
            .defaultMinSize(minWidth = 500.dp, minHeight = 42.dp)
            .sizeIn(maxWidth = 800.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (LocalIsDarkTheme.current)
                    Modifier.shadow(elevation = 2.dp, RoundedCornerShape(8.dp))
                else
                    Modifier.border(1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
            .clickable { state.userInputOpenGameDirPicker() }
            .background(
                if (LocalIsDarkTheme.current)
                    Material3Theme.colorScheme.surfaceContainerHigh
                else
                    Material3Theme.colorScheme.surfaceBright
            )
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp)
                    .defaultMinSize(minWidth = 500.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                run {
                    val color = Material3Theme.colorScheme.onSurface.copy(alpha = 0.78f)
                    Text(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .align(Alignment.CenterVertically),
                        text = state.pickedGameFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/') ?: "Pick game folder",
                        style = Material3Theme.typography.bodyMedium,
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
                Icon(
                    modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
                    painter = painterResource("drawable/icon_folder_96px.png"),
                    contentDescription = null,
                    tint = Material3Theme.colorScheme.secondary
                )
            }
        }
        if (state.pickedGameFolderErr) {
            HeightSpacer(4.dp)
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = state.pickedGameFolderErrMsg,
                color = Material3Theme.colorScheme.error,
                style = Material3Theme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showExample) {
            HeightSpacer(16.dp)
            Row {
                WidthSpacer(4.dp)
                Text(
                    text = buildAnnotatedString {
                        append("example: ")
                        withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                            append(
                                when (state.selectedPlatform) {
                                    "steam" -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/"
                                    "xbox_pc_gamepass" -> "C:/XboxGames/Manor Lords/"
                                    "epic_games_store" -> "C:/Program Files/Epic Games/Manor Lords/"
                                    "gog_com" -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/"
                                    else -> throw RuntimeException("unknown selected platform: ${state.selectedPlatform}")
                                }
                            )
                        }
                    },
                    color = Material3Theme.colorScheme.onSurfaceVariant,
                    style = Material3Theme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SelectGameVersion(
    state: SelectGameWorkingDirectoryState
) {
    /*remember {
        if (state.selectedGameVersion.isEmpty()) {
            state.changeGameVersion(
                when (state.selectedPlatform) {
                    "steam", "epic_games_store", "gog_com" -> "0.8.029a"
                    "xbox_pc_gamepass" -> "0.8.032"
                    else -> throw RuntimeException("unknown selected platform: ${state.selectedPlatform}")
                }
            )
        }
    }*/
    Column {
        Text(
            text = "Select game version",
            color = Material3Theme.colorScheme.onSurface,
            style = Material3Theme.typography.headlineSmall
        )
        HeightSpacer(16.dp)

        Column(modifier = Modifier.widthIn(min = 300.dp, max = 600.dp).width(IntrinsicSize.Max)) {
            run {
                val interactionSource = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier.hoverable(interactionSource),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected platform: ",
                        color = Material3Theme.colorScheme.onSurface,
                        style = Material3Theme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (state.selectedPlatform) {
                            "steam" -> "Steam"
                            "xbox_pc_gamepass" -> "Xbox PC Game Pass"
                            "epic_games_store" -> "Epic Games Store"
                            "gog_com" -> "GOG.com"
                            else -> "Unknown"
                        },
                        color = Material3Theme.colorScheme.onSurface,
                        style = Material3Theme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    WidthSpacer(4.dp)
                    Box(
                        modifier = Modifier.size(20.dp).clickable {
                            state.changePlatform()
                        }
                    ) {
                        if (interactionSource.collectIsHoveredAsState().value) {
                            Icon(
                                painter = painterResource("drawable/icon_edit_feather_outline_24px.png"),
                                contentDescription = null,
                                tint = Material3Theme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            HeightSpacer(4.dp)
            run {
                val interactionSource = remember { MutableInteractionSource() }
                val path = state.pickedGameFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/')
                Row(
                    modifier = Modifier.hoverable(interactionSource),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(Modifier.weight(1f, false)) {
                        Text(
                            text = "Selected install folder: ",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = path.orEmpty(),
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                    state.changeFolder()
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
            HeightSpacer(4.dp)
            run {
                val interactionSource = remember { MutableInteractionSource() }
                val path = state.pickedGameFolder?.absolutePath?.let(::jPath)?.invariantSeparatorsPathString?.ensureSuffix('/')
                Row(
                    modifier = Modifier.hoverable(interactionSource),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(Modifier.weight(1f, false)) {
                        Text(
                            text = "Selected directories: ",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "...",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                    state.changeDirectories()
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
            HeightSpacer(18.dp)
            Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Version",
                    color = Material3Theme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = Material3Theme.typography.bodyMedium
                )
                Spacer(Modifier.width(140.dp).weight(1f, false))
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .width(172.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (state.selectedGameVersion) {
                                "custom" -> "Custom"
                                "0.8.035" -> "0.8.035 (beta)"
                                else -> state.selectedGameVersion
                            },
                            color = Material3Theme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            style = Material3Theme.typography.bodyMedium
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            painter = painterResource("drawable/filled_arrow_head_down_16px.png"),
                            tint = Material3Theme.colorScheme.onSurfaceVariant,
                            contentDescription = null,
                        )
                    }
                    DropdownMenu(
                        expanded,
                        { expanded = false },
                        Modifier.width(140.dp),
                        containerColor = Material3Theme.colorScheme.surfaceContainerHighest,
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            when (state.selectedPlatform) {
                                "steam", "epic_games_store", "gog_com" -> {
                                    Row(
                                        Modifier.height(48.dp)
                                            .fillMaxWidth()
                                            .clickable(state.selectedGameVersion != "0.8.029a") { state.changeGameVersion("0.8.029a")}
                                            .then(if (state.selectedGameVersion == "0.8.029a") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            text = "0.8.029a",
                                            color = Material3Theme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            style = Material3Theme.typography.labelLarge
                                        )
                                        Spacer(Modifier.weight(1f))
                                    }
                                    if (state.selectedPlatform == "steam") {
                                        Row(
                                            Modifier.height(48.dp)
                                                .fillMaxWidth()
                                                .clickable(state.selectedGameVersion != "0.8.035") { state.changeGameVersion("0.8.035") }
                                                .then(if (state.selectedGameVersion == "0.8.035") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                                .padding(horizontal = 12.dp)
                                        ) {
                                            Text(
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                text = "0.8.035 (beta)",
                                                color = Material3Theme.colorScheme.onSurface,
                                                fontWeight = FontWeight.SemiBold,
                                                style = Material3Theme.typography.labelLarge
                                            )
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                                "xbox_pc_gamepass" -> {
                                    Row(
                                        Modifier.height(48.dp)
                                            .fillMaxWidth()
                                            .clickable(state.selectedGameVersion != "0.8.032") { state.changeGameVersion("0.8.032") }
                                            .then(if (state.selectedGameVersion == "0.8.032") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            text = "0.8.032",
                                            color = Material3Theme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            style = Material3Theme.typography.labelLarge
                                        )
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                                else -> runtimeError("unknown selected platform: ${state.selectedPlatform}")
                            }
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(state.selectedGameVersion != "custom") { state.changeGameVersion("custom") }
                                    .then(if (state.selectedGameVersion == "custom") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Custom",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (state.selectedGameVersion == "custom") {
                HeightSpacer(12.dp)
                Box(Modifier.padding(horizontal = 12.dp)) {
                    GameCustomVersionFieldEdit(state)
                }
            }

            HeightSpacer(24.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(
                        if (state.hasGameVersionSelected)
                            Material3Theme.colorScheme.primary
                        else
                            Material3Theme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
                    .clickable(state.hasGameVersionSelected) {
                        state.userSelectGameVersion()
                    }
                    .height(40.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Continue",
                    color = if (state.hasGameVersionSelected)
                        Material3Theme.colorScheme.onPrimary
                    else
                        Material3Theme.colorScheme.onSurface.copy(alpha = 0.38f),
                    style = Material3Theme.typography.labelLarge.copy(fontSize = 16.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
private fun GameCustomVersionFieldEdit(
    state: SelectGameWorkingDirectoryState
) {
    ElevatedCard(
        modifier = Modifier.defaultMinSize(minWidth = 350.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalIsDarkTheme.current) Material3Theme.colorScheme.surfaceContainerHighest
            else Material3Theme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            val textFieldState = remember {
                TextFieldState(state.selectedGameVersionCustom)
            }.also {
                remember(it) {
                    derivedStateOf {
                        val version = it.text.toString()
                        Snapshot.withoutReadObservation { state.changeGameVersionCustom(version) }
                    }
                }.value
            }
            val interactionSource = remember { MutableInteractionSource() }
            val validNotBlank by remember {
                derivedStateOf {
                    val text = textFieldState.text
                    !text.isBlank()
                }
            }
            val isValid by remember {
                derivedStateOf {
                    validNotBlank
                }
            }
            OutlinedTextField(
                modifier = Modifier.height(48.dp).sizeIn(minWidth = 140.dp),
                state = textFieldState,
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = "Custom Version",
                        color = if (interactionSource.collectIsFocusedAsState().value)
                            Material3Theme.colorScheme.primary
                        else
                            Material3Theme.colorScheme.onSurfaceVariant,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                labelPosition = TextFieldLabelPosition.Attached(),
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                isError = !isValid
            )
            HeightSpacer(4.dp)
            Row {
                WidthSpacer(16.dp)
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Not blank")
                        },
                        color = if (validNotBlank)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderPageIndicator(
    state: SelectGameWorkingDirectoryState
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier,
    ) {
        var currentPage = 0
        if (!state.alreadySelectedPlatform)
            currentPage = 0
        else if (!state.alreadySelectedGameFolder)
            currentPage = 1
        else if (!state.alreadySelectedGameDirs)
            currentPage = 2
        else if (!state.alreadySelectedGameVersion)
            currentPage = 3
        for (i in 0 until 4) {
            val isSelected = i == currentPage
            PageIndicatorView(
                isSelected = isSelected,
                selectedColor = Material3Theme.colorScheme.primary,
                defaultColor = Color.LightGray,
                defaultRadius = 8.dp,
                selectedLength = 24.dp,
                animationDurationInMillis = 300,
            )
        }
    }
}

@Composable
private fun PageIndicatorView(
    isSelected: Boolean,
    selectedColor: Color,
    defaultColor: Color,
    defaultRadius: Dp,
    selectedLength: Dp,
    animationDurationInMillis: Int,
    modifier: Modifier = Modifier,
) {
    val color: Color by animateColorAsState(
        targetValue = if (isSelected) {
            selectedColor
        } else {
            defaultColor
        },
        animationSpec = tween(
            durationMillis = animationDurationInMillis,
        )
    )
    val width: Dp by animateDpAsState(
        targetValue = if (isSelected) {
            selectedLength
        } else {
            defaultRadius
        },
        animationSpec = tween(
            durationMillis = animationDurationInMillis,
        )
    )

    Canvas(
        modifier = modifier
            .size(
                width = width,
                height = defaultRadius,
            ),
    ) {
        drawRoundRect(
            color = color,
            topLeft = Offset.Zero,
            size = Size(
                width = width.toPx(),
                height = defaultRadius.toPx(),
            ),
            cornerRadius = CornerRadius(
                x = defaultRadius.toPx(),
                y = defaultRadius.toPx(),
            ),
        )
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleTooltip(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    TooltipArea(
        delayMillis = 300,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Default),
        tooltip = {
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 24.dp)
                    .background(Material3Theme.colorScheme.inverseSurface)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text,
                    color = Material3Theme.colorScheme.inverseOnSurface,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = maxLines
                )
            }
        }
    ) {
        content()
    }
}