package dev.psiae.mltoolbox.feature.modmanager.ui.compose.setup

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.core.utils.ensureSuffix
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.platformSlashSeparated
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toFsPath
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toPath
import dev.psiae.mltoolbox.foundation.fs.path.endsWith
import dev.psiae.mltoolbox.foundation.fs.path.invariantSeparatorsPathString
import dev.psiae.mltoolbox.foundation.fs.path.startsWith
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.LocalAwtWindow
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf
import dev.psiae.mltoolbox.foundation.ui.compose.visibilityGone
import dev.psiae.mltoolbox.foundation.ui.compose.visibilityInvisible
import dev.psiae.mltoolbox.shared.domain.model.GamePlatform
import dev.psiae.mltoolbox.shared.domain.model.ManorLordsGameVersion
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.IllegalStateException

@Composable
fun SetupGameContextPanel(
    state: SetupGameContextPanelState
) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 48.dp)
            .sizeIn(maxWidth = 1400.dp, maxHeight = 1400.dp),
        colors = CardDefaults
            .cardColors(
                containerColor = Material3Theme.colorScheme.surfaceContainer,
                contentColor = Material3Theme.colorScheme.onSurface
            )
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (!state.alreadySelectedPlatform) {
                SelectGamePlatform(state::selectGamePlatform)
            } else if (!state.alreadySelectedGameInstallFolder) {
                SelectGameInstallFolder(state)
            } else if (!state.alreadySelectedGamePaths) {
                SelectGamePaths(state)
            } else if (!state.alreadySelectedGameVersion) {
                SelectGameVersion(state)
            }
            HeightSpacer(28.dp)
            Row {
                if (!state.alreadySelectedPlatform || !state.alreadySelectedGameInstallFolder || !state.alreadySelectedGamePaths || !state.alreadySelectedGameVersion)
                    RenderPageIndicator(
                        state.alreadySelectedPlatform,
                        state.alreadySelectedGameInstallFolder,
                        state.alreadySelectedGamePaths,
                        state.alreadySelectedGameVersion,
                    )
            }
        }
    }
}
@Composable
private fun RenderPageIndicator(
    alreadySelectedPlatform: Boolean,
    alreadySelectedGameFolder: Boolean,
    alreadySelectedGameDirs: Boolean,
    alreadySelectedGameVersion: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier,
    ) {
        var currentPage = 0
        if (!alreadySelectedPlatform)
            currentPage = 0
        else if (!alreadySelectedGameFolder)
            currentPage = 1
        else if (!alreadySelectedGameDirs)
            currentPage = 2
        else if (!alreadySelectedGameVersion)
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

@Composable
private fun SelectGamePlatform(
    selectPlatform: (GamePlatform) -> Unit
) {
    var currentlySelectedPlatform by remember {
        mutableStateOf<GamePlatform?>(null)
    }
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
            mutableListOf<GamePlatform>(
                GamePlatform.Steam, GamePlatform.XboxPcGamePass, GamePlatform.EpicGamesStore, GamePlatform.GogCom
            )
        }
        val selectedPlatformIndex by remember {
            derivedStateOf { selections.indexOf(currentlySelectedPlatform) }
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
        val focusRequesterGogCom = remember {
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
                    when (event.type) {
                        KeyEventType.KeyDown -> {
                            when(event.key) {
                                Key.DirectionDown, Key.DirectionUp -> {
                                    val nextIndex = focusRequesterIndex
                                        .plus(if (event.key == Key.DirectionDown) 1 else -1)
                                        .coerceAtMost(selections.lastIndex)
                                    if (nextIndex >= 0) {
                                        focusRequesters[nextIndex].requestFocus()
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                        else -> false
                    }
                }.onFocusChanged {
                    if (!it.hasFocus)
                        focusRequesterIndex = -1
                }.focusGroup()
        ) {
            val isSteamSelected = currentlySelectedPlatform == GamePlatform.Steam
            val isSteamFocused = focusRequesterIndex == focusRequesters.indexOf(focusRequesterSteam)
            SelectableGamePlatform(
                "Steam",
                painterResource("drawable/icon_steam_logo_convert_32px.png"),
                isSelected = isSteamSelected,
                focusRequester = focusRequesterSteam,
                isFocused = isSteamFocused,
                onFocusChanged = { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterSteam) },
                onClick = { selectPlatform(GamePlatform.Steam) },
            )
            val isXboxSelected = currentlySelectedPlatform == GamePlatform.XboxPcGamePass
            val isXboxFocused = focusRequesterIndex == focusRequesters.indexOf(focusRequesterXbox)
            SelectableGamePlatform(
                "Xbox PC Game Pass",
                painterResource("drawable/icon_xbox_logo_convert_32px.png"),
                isSelected = isXboxSelected,
                focusRequester = focusRequesterXbox,
                isFocused = isXboxFocused,
                onFocusChanged = { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterXbox) },
                onClick = { selectPlatform(GamePlatform.XboxPcGamePass) },
            )
            val isGogComSelected = currentlySelectedPlatform == GamePlatform.GogCom
            val isGogComFocused = focusRequesterIndex == focusRequesters.indexOf(focusRequesterGogCom)
            SelectableGamePlatform(
                "GOG.com",
                painterResource("drawable/GOG.com_logo_44x42.png"),
                if (LocalIsDarkTheme.current) Color.White else Color.Black,
                isSelected = isGogComSelected,
                focusRequester = focusRequesterGogCom,
                isFocused = isGogComFocused,
                onFocusChanged = { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterGogCom) },
                onClick = { selectPlatform(GamePlatform.GogCom) },
            )
            val isEpicSelected = currentlySelectedPlatform == GamePlatform.EpicGamesStore
            val isEpicFocused = focusRequesterIndex == focusRequesters.indexOf(focusRequesterEpic)
            SelectableGamePlatform(
                "Epic Games Store",
                if (LocalIsDarkTheme.current)
                    painterResource("drawable/EGS-Logotype-2023-Horizontal-White_106x38.png")
                else
                    painterResource("drawable/EGS-Logotype-2023-Horizontal-Black_106x38.png"),
                padding = PaddingValues(horizontal = 10.dp, 12.dp),
                isSelected = isEpicSelected,
                focusRequester = focusRequesterEpic,
                isFocused = isEpicFocused,
                onFocusChanged = { if (it.hasFocus) focusRequesterIndex = focusRequesters.indexOf(focusRequesterEpic) },
                onClick = { selectPlatform(GamePlatform.EpicGamesStore) },
            )
        }
    }
}

@Composable
private fun SelectableGamePlatform(
    label: String,
    iconPainter: Painter,
    iconTint: Color = Color.Unspecified,
    padding: PaddingValues = PaddingValues(horizontal = 8.dp, 12.dp),
    focusRequester: FocusRequester,
    isSelected: Boolean,
    isFocused: Boolean,
    onFocusChanged: (FocusState) -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .thenIf(isSelected) {
                Modifier.background(Material3Theme.colorScheme.secondaryContainer)
            }
            .focusRequester(focusRequester)
            .onFocusChanged(onFocusChanged)
            .thenIf(isFocused) {
                Modifier.background(Material3Theme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            }
            .clickable(onClick = onClick)
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.height(42.dp)) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                painter = iconPainter,
                contentDescription = null,
                tint = iconTint
            )
        }
        WidthSpacer(12.dp)
        Text(
            text = buildAnnotatedString {
                withStyle(
                    Material3Theme.typography.bodyLarge.toSpanStyle()
                        .copy(fontWeight = FontWeight.Medium)
                ) {
                    append(label)
                }
            },
            color = Material3Theme.colorScheme.onSurface,
            maxLines = 1
        )
        WidthSpacer(24.dp)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SelectGameInstallFolder(
    state: SetupGameContextPanelState
) {
    Column {
        Text(
            text = "Select game install folder",
            color = Material3Theme.colorScheme.onSurface,
            style = Material3Theme.typography.headlineSmall
        )
        HeightSpacer(16.dp)
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier.heightIn(min = 150.dp).widthIn(min = 450.dp, max = 600.dp).width(IntrinsicSize.Max)
                    .thenIf(state.isProcessingGamePickedFolder) {
                        Modifier.visibilityInvisible()
                    }
            ) {

                Box {
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
                            text = state.selectedGamePlatformOrThrow().label,
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
                                    .clickable(enabled = !state.isProcessingGamePickedFolder, onClick = state::changeGamePlatform)
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
                        PickFolderWidget(
                            selectedPlatform = state.selectedGamePlatformOrThrow(),
                            enabled = !state.isProcessingGamePickedFolder,
                            isError = state.pickedGameFolderErr,
                            getErrorMessage = state::pickedGameFolderErrMsg::get,
                            onBeginFolderPick = state::onBeginFolderPick,
                            showExample = true,
                            onFolderPick = state::onGameInstallFolderPicked
                        )
                    }
                }
            }
            if (state.isProcessingGamePickedFolder) {
                Column(
                    modifier = Modifier
                        .heightIn(min = 150.dp)
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ContainedLoadingIndicator(
                        modifier = Modifier
                            .size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PickFolderWidget(
    selectedPlatform: GamePlatform,
    enabled: Boolean,
    isError: Boolean,
    getErrorMessage: () -> String,
    onBeginFolderPick: () -> Unit,
    showExample: Boolean = false,
    onFolderPick: (Path?) -> Unit
) {
    var openFolderPicker by remember { mutableStateOf(false) }
        .apply {
            if (!enabled)
                value = false
        }
    if (openFolderPicker)
        LaunchGameFolderPicker(
            Path.EMPTY,
            filePick = {
                openFolderPicker = false
                onFolderPick(it)
            }
        )
    Column {
        Box(modifier = Modifier
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
            .clickable(enabled) {
                openFolderPicker = true
                onBeginFolderPick()
            }
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
                        text = "Pick game folder",
                        style = Material3Theme.typography.bodyMedium,
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                WidthSpacer(8.dp)
                Icon(
                    modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
                    painter = painterResource("drawable/icon_folder_96px.png"),
                    contentDescription = null,
                    tint = Material3Theme.colorScheme.secondary
                )
            }
        }
        if (isError) {
            HeightSpacer(4.dp)
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = getErrorMessage(),
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
                                when (selectedPlatform) {
                                    GamePlatform.Steam -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/"
                                    GamePlatform.XboxPcGamePass -> "C:/XboxGames/Manor Lords/"
                                    GamePlatform.EpicGamesStore -> "C:/Program Files/Epic Games/Manor Lords/"
                                    GamePlatform.GogCom -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/"
                                    /*else -> throw RuntimeException("unknown selected platform: ${selectedPlatform}")*/
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
private fun LaunchGameFolderPicker(
    initialDirectory: Path,
    filePick: (Path?) -> Unit
) {
    val window = LocalAwtWindow.current
    LaunchedEffect(Unit) {
        val pick = FileKit.pickDirectory(
            title = "Select game folder (Manor Lords)",
            platformSettings = FileKitPlatformSettings(parentWindow = window),
            initialDirectory = initialDirectory.toString()
        )
        ensureActive()
        pick?.file?.toFsPath().let(filePick)
    }
}

@Composable
private fun SelectGamePaths(
    state: SetupGameContextPanelState
) {
    val isLoading by derivedStateOf { state.isProcessingGamePickedFolder }
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
                    .thenIf(showLoading) {
                        Modifier.visibilityGone()
                    }
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
                            text = state.selectedGamePlatformOrThrow().label,
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
                                        state.changeGamePlatform()
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
                    val path = with(state.fs) {
                        state.selectedGamePathsOrThrow().install.absolute().invariantSeparatorsPathString.ensureSuffix('/')
                    }
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
                                text = path,
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
                                        state.changeGameInstallFolder()
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
                        else -> throw IllegalStateException("unknown editField: $editFieldKey")
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
                        .clickable(state.hasAllDirectoriesSelected, onClick = state::confirmSelectPaths)
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
    state: SetupGameContextPanelState,
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
                path = with(state.fs) {
                    val path = state.selectedGamePathsOrThrow().root
                    if (path.isEmpty)
                        ""
                    else
                        path.absolute().invariantSeparatorsPathString.ensureSuffix('/')
                },
                edit = {editField("game_root_folder") },
            )
            GameLauncherExeField(
                path = with(state.fs) {
                    val path = state.selectedGamePathsOrThrow().launcher
                    if (path.isEmpty)
                        ""
                    else
                        path.absolute().invariantSeparatorsPathString
                },
                edit = {editField("game_launcher_exe") },
            )
            GameBinaryExeField(
                path = with(state.fs) {
                    val path = state.selectedGamePathsOrThrow().binary
                    if (path.isEmpty)
                        ""
                    else
                        path.absolute().invariantSeparatorsPathString
                },
                edit = {editField("game_binary_exe") },
            )
            GamePaksFolderField(
                path = with(state.fs) {
                    val path = state.selectedGamePathsOrThrow().paks
                    if (path.isEmpty)
                        ""
                    else
                        path.absolute().invariantSeparatorsPathString.ensureSuffix('/')
                },
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
private fun GameDirectoriesPathField(
    path: String,
    label: String,
    edit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.hoverable(interactionSource),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f, false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                color = Material3Theme.colorScheme.onSurface,
                style = Material3Theme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = path,
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
private fun GameRootFolderField(
    path: String,
    edit: () -> Unit
) {
    GameDirectoriesPathField(path, "Game Root folder", edit)
}
@Composable
private fun GameLauncherExeField(
    path: String,
    edit: () -> Unit
) {
    GameDirectoriesPathField(path, "Game Launcher exe", edit)
}


@Composable
private fun GameBinaryExeField(
    path: String,
    edit: () -> Unit
) {
    GameDirectoriesPathField(path, "Game Binary exe", edit)
}
@Composable
private fun GamePaksFolderField(
    path: String,
    edit: () -> Unit
) {
    GameDirectoriesPathField(path, "Game Paks folder", edit)
}

@Composable
private fun GameDirectoriesPathFieldEdit(
    isValid: Boolean,
    textField: @Composable () -> Unit,
    example: String,
    cancel: () -> Unit,
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
            textField()


            HeightSpacer(16.dp)

            Text(
                text = buildAnnotatedString {
                    append("example: ")
                    withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                        append(example)
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
                    onClick = cancel,
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
                    onClick = done,
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
private fun GamePathFieldEdit(
    state: SetupGameContextPanelState,
    initialPath: Path,
    initialPathIsDirectory: Boolean,
    requireIsFile: Boolean,
    requireIsDirectory: Boolean,
    requireStartsWith: Path,
    requireStartsWithIsDirectory: Boolean,
    requireEndsWith: Path,
    requireEndsWithIsExtension: Boolean,
    requireEndsWithIsDirectory: Boolean,
    label: String,
    example: String,
    cancel: () -> Unit,
    done: (Path) -> Unit
) {
    require(!(requireIsFile && requireIsDirectory)) {
        "Cannot require is file and directory"
    }
    require(!(requireEndsWithIsExtension && requireEndsWithIsDirectory)) {
        "Cannot require ends with file extension and directory"
    }

    val textFieldState = remember {
        TextFieldState(
            with(state.fs) {
                if (initialPath.isEmpty)
                    ""
                else
                    initialPath.absolute().invariantSeparatorsPathString.let {
                        if (initialPathIsDirectory) it.ensureSuffix('/') else it
                    }
            }
        )
    }
    var textChangedKey by remember {
        mutableStateOf(Any())
    }
    val lastText by remember {
        mutableStateOf(textFieldState.text, neverEqualPolicy())
    }.apply {
        if (value != textFieldState.text) {
            value = textFieldState.text
            textChangedKey = Any()
        }
    }
    val latestRequireStartsWith by rememberUpdatedState(requireStartsWith)
    val isValidStartsWith by remember {
        derivedStateOf {
            val textStr = textFieldState.text
            if (textStr.isBlank())
                return@derivedStateOf false
            latestRequireStartsWith.isEmpty ||
                textStr.toString().toPath().platformSlashSeparated().startsWith(latestRequireStartsWith.platformSlashSeparated())
        }
    }
    val latestRequireEndsWith by rememberUpdatedState(requireEndsWith)
    val latestRequireEndsWithIsExtension by rememberUpdatedState(requireEndsWithIsExtension)
    val isValidEndsWith by remember {
        derivedStateOf {
            val text = textFieldState.text
            val textStr = text.toString()
            if (textStr.isBlank())
                return@derivedStateOf false
            val path = textStr.toPath(true)
            latestRequireEndsWith.isEmpty ||
                if (latestRequireEndsWithIsExtension) {
                    textStr.endsWith(latestRequireEndsWith.toString()) &&
                        textStr
                            .takeLastWhile { it != '\\' && it != '/' }
                            .isNotEmpty()
                } else {
                    path.platformSlashSeparated().endsWith(latestRequireEndsWith.platformSlashSeparated())
                }
        }
    }
    val isValidFileType by remember(
        requireIsFile,
        requireIsDirectory,
        textChangedKey
    ) {
        mutableStateOf(false)
    }.apply {
        LaunchedEffect(this) {
            runCatching {
                if (requireIsFile || requireIsDirectory)
                    withContext(state.ioDispatcher) {
                        value = with(state.fs) {
                            val path = textFieldState.text.toString().toPath()
                            isValidPath(path) &&
                                path.file().let { if (requireIsFile) it.isRegularFile() else it.isDirectory() }
                        }
                    }
            }.catchOrRethrow { e ->
                if (e is IOException)
                    return@LaunchedEffect
            }
        }
    }

    val isValid = isValidStartsWith and isValidEndsWith and isValidFileType
    val textField = @Composable {
        val interactionSource = remember { MutableInteractionSource() }
        OutlinedTextField(
            modifier = Modifier.height(48.dp).sizeIn(minWidth = 600.dp),
            state = textFieldState,
            interactionSource = interactionSource,
            label = {
                Text(
                    text = label,
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
                if (!requireStartsWith.isEmpty) {
                    Text(
                        text = buildAnnotatedString {
                            append("Starts with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(with(state.fs) {
                                    requireStartsWith.absolute().invariantSeparatorsPathString.let {
                                        if (requireStartsWithIsDirectory)
                                            it.ensureSuffix('/')
                                        else it
                                    }
                                })
                            }
                        },
                        color = if (isValidStartsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!requireEndsWith.isEmpty) {
                    Text(
                        text = buildAnnotatedString {
                            append("Ends with ")
                            withStyle(Material3Theme.typography.labelMedium.toSpanStyle()) {
                                append(with(state.fs) {
                                    requireEndsWith.invariantSeparatorsPathString.let {
                                        if (requireEndsWithIsDirectory)
                                            it.ensureSuffix('/')
                                        else it
                                    }
                                })
                            }
                        },
                        color = if (isValidEndsWith)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (requireIsFile) {
                    Text(
                        text = buildAnnotatedString {
                            append("Is File")
                        },
                        color = if (isValidFileType)
                            Material3Theme.colorScheme.onSurfaceVariant
                        else
                            Material3Theme.colorScheme.error,
                        style = Material3Theme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (requireIsDirectory) {
                    Text(
                        text = buildAnnotatedString {
                            append("Is Folder")
                        },
                        color = if (isValidFileType)
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
    GameDirectoriesPathFieldEdit(
        isValid,
        textField,
        example,
        cancel,
    ) {
        done(textFieldState.text.toString().toPath().platformSlashSeparated())
    }
}

@Composable
private fun GameRootFolderFieldEdit(
    state: SetupGameContextPanelState,
    done: () -> Unit
) = GamePathFieldEdit(
    state = state,
    initialPath = state.selectedGamePathsOrThrow().root,
    initialPathIsDirectory = true,
    requireIsFile = false,
    requireIsDirectory = true,
    requireStartsWith = state.selectedGamePathsOrThrow().install,
    requireStartsWithIsDirectory = true,
    requireEndsWith = Path.EMPTY,
    requireEndsWithIsExtension = false,
    requireEndsWithIsDirectory = false,
    label = "Game Root folder path",
    example = when (state.selectedGamePlatformOrThrow()) {
        GamePlatform.Steam -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/"
        GamePlatform.XboxPcGamePass -> "C:/XboxGames/Manor Lords/Content/"
        GamePlatform.EpicGamesStore -> "C:/Program Files/Epic Games/Manor Lords/"
        GamePlatform.GogCom -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/"
    },
    cancel = done
) {
    state.selectGameRootFolder(it)
    done()
}

@Composable
private fun GameLauncherExeFieldEdit(
    state: SetupGameContextPanelState,
    done: () -> Unit
) = GamePathFieldEdit(
    state = state,
    initialPath = state.selectedGamePathsOrThrow().launcher,
    initialPathIsDirectory = false,
    requireIsFile = true,
    requireIsDirectory = false,
    requireStartsWith = state.selectedGamePathsOrThrow().install,
    requireStartsWithIsDirectory = true,
    requireEndsWith = remember { ".exe".toPath() },
    requireEndsWithIsExtension = true,
    requireEndsWithIsDirectory = false,
    label = "Game Launcher exe path",
    example = when (state.selectedGamePlatformOrThrow()) {
        GamePlatform.Steam -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/ManorLords.exe"
        GamePlatform.XboxPcGamePass -> "C:/XboxGames/Manor Lords/Content/gamelaunchhelper.exe"
        GamePlatform.EpicGamesStore -> "C:/Program Files/Epic Games/Manor Lords/ManorLords/ManorLords.exe"
        GamePlatform.GogCom -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/ManorLords/ManorLords.exe"
    },
    cancel = done
) {
    state.selectGameLauncherFile(it)
    done()
}

@Composable
private fun GameBinaryExeFieldEdit(
    state: SetupGameContextPanelState,
    done: () -> Unit
) = GamePathFieldEdit(
    state = state,
    initialPath = state.selectedGamePathsOrThrow().binary,
    initialPathIsDirectory = false,
    requireIsFile = true,
    requireIsDirectory = false,
    requireStartsWith = state.selectedGamePathsOrThrow().install,
    requireStartsWithIsDirectory = true,
    requireEndsWith = remember { ".exe".toPath() },
    requireEndsWithIsExtension = true,
    requireEndsWithIsDirectory = false,
    label = "Game Binary exe path",
    example = when (state.selectedGamePlatformOrThrow()) {
        GamePlatform.Steam -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/ManorLords/Binaries/Win64/ManorLords-Win64-Shipping.exe"
        GamePlatform.XboxPcGamePass -> "C:/XboxGames/Manor Lords/Content/ManorLords/Binaries/WinGDK/ManorLords-WinGDK-Shipping.exe"
        GamePlatform.EpicGamesStore -> "C:/Program Files/Epic Games/Manor Lords/ManorLords/Binaries/Win64/ManorLords-Win64-Shipping.exe"
        GamePlatform.GogCom -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/ManorLords/Binaries/Win64/ManorLords-Win64-Shipping.exe"
    },
    cancel = done
) {
    state.selectGameBinaryFile(it)
    done()
}

@Composable
private fun GamePaksFolderFieldEdit(
    state: SetupGameContextPanelState,
    done: () -> Unit
) = GamePathFieldEdit(
    state = state,
    initialPath = state.selectedGamePathsOrThrow().paks,
    initialPathIsDirectory = true,
    requireIsFile = false,
    requireIsDirectory = true,
    requireStartsWith = state.selectedGamePathsOrThrow().install,
    requireStartsWithIsDirectory = true,
    requireEndsWith = Path.EMPTY,
    requireEndsWithIsExtension = false,
    requireEndsWithIsDirectory = false,
    label = "Game Paks folder path",
    example = when (state.selectedGamePlatformOrThrow()) {
        GamePlatform.Steam -> "C:/Program Files (x86)/Steam/steamapps/common/Manor Lords/ManorLords/Content/Paks"
        GamePlatform.XboxPcGamePass -> "C:/XboxGames/Manor Lords/Content/ManorLords/Content/Paks"
        GamePlatform.EpicGamesStore -> "C:/Program Files/Epic Games/Manor Lords/ManorLords/Content/Paks"
        GamePlatform.GogCom -> "C:/Program Files (x86)/GOG Galaxy/Games/Manor Lords/ManorLords/Content/Paks"
    },
    cancel = done
) {
    state.selectGamePaksFolder(it)
    done()
}

@Composable
private fun SelectGameVersion(
    state: SetupGameContextPanelState
) {
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
                        text = state.selectedGamePlatformOrThrow().label,
                        color = Material3Theme.colorScheme.onSurface,
                        style = Material3Theme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    WidthSpacer(4.dp)
                    Box(
                        modifier = Modifier.size(20.dp).clickable {
                            state.changeGamePlatform()
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
                val path = state.selectedGamePathsOrThrow().install.let {
                    if (it.isEmpty)
                        ""
                    else
                        with(state.fs) {
                            it.absolute().invariantSeparatorsPathString.ensureSuffix('/')
                        }
                }
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
                            text = path,
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
                                    state.changeGameInstallFolder()
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
                                    state.changeGamePaths()
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
                            .background(Material3Theme.colorScheme.surfaceContainerHighest)
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (state.selectedGameVersion) {
                                is ManorLordsGameVersion.Custom -> "Custom"
                                ManorLordsGameVersion.V_0_8_050-> "0.8.050 (Beta)"
                                ManorLordsGameVersion.V_0_8_029a -> "0.8.029a"
                                else -> state.selectedGameVersion?.versionStr ?: ""
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
                            when (val platform = state.selectedGamePlatformOrThrow()) {
                                GamePlatform.Steam, GamePlatform.GogCom, GamePlatform.EpicGamesStore -> {
                                    if (platform == GamePlatform.Steam) {
                                        Row(
                                            Modifier.height(48.dp)
                                                .fillMaxWidth()
                                                .clickable(state.selectedGameVersion != ManorLordsGameVersion.V_0_8_050) {
                                                    state.selectGameVersion(ManorLordsGameVersion.V_0_8_050)
                                                }
                                                .then(if (state.selectedGameVersion == ManorLordsGameVersion.V_0_8_050) Modifier.background(
                                                    Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                                .padding(horizontal = 12.dp)
                                        ) {
                                            Text(
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                text = "0.8.050 (Beta)",
                                                color = Material3Theme.colorScheme.onSurface,
                                                fontWeight = FontWeight.SemiBold,
                                                style = Material3Theme.typography.labelLarge
                                            )
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                    Row(
                                        Modifier.height(48.dp)
                                            .fillMaxWidth()
                                            .clickable(state.selectedGameVersion != ManorLordsGameVersion.V_0_8_029a) {
                                                state.selectGameVersion(ManorLordsGameVersion.V_0_8_029a)
                                            }
                                            .then(if (state.selectedGameVersion == ManorLordsGameVersion.V_0_8_029a) Modifier.background(
                                                Material3Theme.colorScheme.secondaryContainer) else Modifier)
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
                                }
                                GamePlatform.XboxPcGamePass -> {
                                    Row(
                                        Modifier.height(48.dp)
                                            .fillMaxWidth()
                                            .clickable(state.selectedGameVersion != ManorLordsGameVersion.V_0_8_032) {
                                                state.selectGameVersion(ManorLordsGameVersion.V_0_8_032)
                                            }
                                            .then(if (state.selectedGameVersion == ManorLordsGameVersion.V_0_8_032) Modifier.background(
                                                Material3Theme.colorScheme.secondaryContainer) else Modifier)
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
                            }
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(state.selectedGameVersion !is ManorLordsGameVersion.Custom) {
                                        state.selectGameVersion(ManorLordsGameVersion.Custom(""))
                                    }
                                    .then(
                                        if (state.selectedGameVersion is ManorLordsGameVersion.Custom)
                                            Modifier.background(Material3Theme.colorScheme.secondaryContainer)
                                        else
                                            Modifier
                                    )
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

            if (state.selectedGameVersion is ManorLordsGameVersion.Custom) {
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
                        state.confirmSelectGameVersion()
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
fun GameCustomVersionFieldEdit(
    state: SetupGameContextPanelState
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
                TextFieldState((state.selectedGameVersionOrThrow() as ManorLordsGameVersion.Custom).customVersionStr)
            }.apply {
                remember {
                    derivedStateOf {
                        val version = text.toString()
                        Snapshot.withoutReadObservation { state.selectGameVersion(ManorLordsGameVersion.Custom(version)) }
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