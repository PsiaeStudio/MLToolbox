package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.SimpleTooltip
import dev.psiae.mltoolbox.shared.ui.composeui.HeightSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.LocalAwtWindow
import dev.psiae.mltoolbox.shared.ui.composeui.WidthSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.ripple
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.surfaceColorAtElevation
import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.logger.Logger
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import kotlinx.coroutines.launch
import java.net.URI

@Composable
fun DirectInstallUE4SSModScreen(
    directInstallModScreenState: DirectInstallModScreenState
) {
    val ue4ssState = rememberDirectInstallUE4SSModScreenState(directInstallModScreenState)
    val window = LocalAwtWindow.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        val snackbar = remember { SnackbarHostState() }
        val scrollState = rememberScrollState()
        val topBarScrolling by remember { derivedStateOf { scrollState.value > 0 } }
        CompositionLocalProvider(
            LocalIndication provides MD3Theme.ripple(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MD3Theme.surfaceColorAtElevation(
                                surface = Material3Theme.colorScheme.surface,
                                elevation = if (topBarScrolling) 2.dp else 0.dp,
                                tint = Material3Theme.colorScheme.primary
                            )
                        )
                ) {
                    Column(
                        Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SimpleTooltip(
                                "go back"
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .clickable(onClick = { directInstallModScreenState.userInputNavigateOutInstallUE4SSMod() })
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource("drawable/arrow_left_simple_32px.png"),
                                            tint = Material3Theme.colorScheme.onSurface,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                "Install RE-UE4SS Mod",
                                style = Material3Theme.typography.headlineMedium,
                                color = Material3Theme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Row {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Column {
                            HeightSpacer(16.dp)
                            Text(
                                text = "RE-UE4SS Mod Installation",
                                style = Material3Theme.typography.titleLarge,
                                color = Material3Theme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            HeightSpacer(8.dp)
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("1. Import the mod(s) archive below")
                                        withStyle(Material3Theme.typography.bodySmall.toSpanStyle()) {
                                            append("\n\n")
                                            append("*note: previous installation will be deleted")
                                            append("\n")
                                            append("*note: make sure RE-UE4SS Mod Loader is already installed")
                                        }
                                    },
                                    style = Material3Theme.typography.bodyLarge.copy(color = Material3Theme.colorScheme.onSurface)
                                )
                            }
                            HeightSpacer(32.dp)
                            SelectModArchiveCard(Modifier, ue4ssState, snackbar)
                            Box(
                                modifier = Modifier.height(16.dp)
                            )
                        }
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
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SelectModArchiveCard(
    modifier: Modifier,
    ue4ssState: DirectInstallUE4SSModScreenState,
    snackbar: SnackbarHostState
) {
    val window = LocalAwtWindow.current
    Column(
        modifier = modifier
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val draggingInBoundState = remember {
                mutableStateOf(false)
            }
            val showInBoundEffect = true
            val leastConstraintsMax = minOf(maxWidth, maxHeight)
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(
                        if (LocalIsDarkTheme.current)
                            Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                        else
                            Modifier
                    )
                /*.verticalScroll(rememberScrollState())*/,
                colors = CardDefaults.cardColors(containerColor = Material3Theme.colorScheme.surfaceContainerHigh, contentColor = Material3Theme.colorScheme.onSurface)
            ) {
                val contentMinSize = when {
                    leastConstraintsMax < 1000.dp -> 300.dp
                    else -> 450.dp
                }
                if (ue4ssState.isLoading) {
                    Column(
                        Modifier
                            .defaultMinSize(contentMinSize, contentMinSize)
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
                            text = ue4ssState.statusMessage ?: "",
                            style = Material3Theme.typography.titleMedium,
                            color = Material3Theme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                } else if (ue4ssState.selectedUE4SSModsArchive == null) {
                    val dragAndDropTarget = remember {
                        object : DragAndDropTarget {
                            override fun onEntered(event: DragAndDropEvent) {
                                draggingInBoundState.value = true
                            }

                            override fun onExited(event: DragAndDropEvent) {
                                draggingInBoundState.value = false
                            }

                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                draggingInBoundState.value = false
                                ue4ssState.userDropUE4SSModsArchive(
                                    (event.dragData() as DragData.FilesList).readFiles().map {
                                        jFile(URI(it))
                                    }
                                )
                                return true
                            }
                        }
                    }
                    @OptIn(ExperimentalFoundationApi::class)
                    Column(
                        Modifier
                            .defaultMinSize(contentMinSize, contentMinSize)
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { event ->
                                    val dragData = event.dragData()
                                    val accept = dragData is DragData.FilesList &&
                                        dragData.readFiles().let { files ->
                                            files.isNotEmpty() && files.all {
                                                jFile(URI(it)).isFile && (it.endsWith(".zip", ignoreCase = true) ||
                                                        it.endsWith(".rar", ignoreCase = true) ||
                                                        it.endsWith(".7z", ignoreCase = true))
                                            }
                                        }
                                    return@dragAndDropTarget accept
                                },
                                target = dragAndDropTarget
                            )
                            .then(
                                if (draggingInBoundState.value && showInBoundEffect) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = Color.Green,
                                        shape = RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                } else Modifier
                            )
                            .clickable { ue4ssState.pickUE4SSArchive(window) }
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.padding(12.dp).size(128.dp),
                            painter = painterResource("drawable/icon_import_mm_128px.png"),
                            contentDescription = null,
                            tint = Material3Theme.colorScheme.secondary
                        )
                        HeightSpacer(12.dp)
                        Text(
                            text = "Select or Drop the archive (*.zip, *.rar, *.7z) here",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyMedium
                        )

                        if (ue4ssState.isLastSelectedArchiveInvalid) {
                            HeightSpacer(16.dp)
                            val ins =
                                remember { MutableInteractionSource() }
                            val clipBoardManager =
                                LocalClipboardManager.current
                            val text = buildAnnotatedString {
                                append("[Error][invalid archive]: ${ue4ssState.statusMessage}")
                            }
                            val coroutineScope = rememberCoroutineScope()
                            SimpleTooltip("click to copy") {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Material3Theme.colorScheme.error)
                                        .clickable(
                                            interactionSource = ins,
                                            indication = LocalIndication.current
                                        ) {
                                            clipBoardManager.setText(
                                                annotatedString = text
                                            )
                                            coroutineScope.launch {
                                                snackbar.currentSnackbarData?.dismiss()
                                                snackbar.showSnackbar(
                                                    message = "Copied to clipboard",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f, false),
                                        text = text,
                                        color = Material3Theme.colorScheme.onError,
                                        style = Material3Theme.typography.bodyMedium,
                                    )
                                    Box(modifier = Modifier.size(32.dp).align(Alignment.Top)) {
                                        if (/*ins.collectIsHoveredAsState().value*/ true) {
                                            Icon(
                                                modifier = Modifier.size(24.dp).align(Alignment.Center),
                                                painter = painterResource("drawable/icon_copy_24px.png"),
                                                contentDescription = null,
                                                tint = Material3Theme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (ue4ssState.isInvalidModsDirectory) {
                            HeightSpacer(16.dp)
                            val ins =
                                remember { MutableInteractionSource() }
                            val clipBoardManager =
                                LocalClipboardManager.current
                            val text = buildAnnotatedString {
                                append("[Error][invalid mods dir]: ${ue4ssState.statusMessage}")
                            }
                            val coroutineScope = rememberCoroutineScope()
                            SimpleTooltip("click to copy") {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Material3Theme.colorScheme.error)
                                        .clickable(
                                            interactionSource = ins,
                                            indication = LocalIndication.current
                                        ) {
                                            clipBoardManager.setText(
                                                annotatedString = text
                                            )
                                            coroutineScope.launch {
                                                snackbar.currentSnackbarData?.dismiss()
                                                snackbar.showSnackbar(
                                                    message = "Copied to clipboard",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f, false),
                                        text = text,
                                        color = Material3Theme.colorScheme.onError,
                                        style = Material3Theme.typography.bodyMedium,
                                    )
                                    Box(modifier = Modifier.size(32.dp).align(Alignment.Top)) {
                                        if (ins.collectIsHoveredAsState().value) {
                                            Icon(
                                                modifier = Modifier.size(24.dp).align(Alignment.Center),
                                                painter = painterResource("drawable/icon_copy_24px.png"),
                                                contentDescription = null,
                                                tint = Material3Theme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (ue4ssState.isInstalledSuccessfully) {
                            HeightSpacer(24.dp)
                            val text = buildAnnotatedString {
                                append("Installed Successfully !")
                            }
                            Text(
                                modifier = Modifier.weight(1f, false),
                                text = text,
                                color = Material3Theme.colorScheme.primary,
                                style = Material3Theme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

    }
}