package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.LocalAwtWindow
import dev.psiae.mltoolbox.foundation.ui.compose.graphics.absoluteContentColor
import dev.psiae.mltoolbox.foundation.ui.compose.graphics.surfaceColorAtElevation
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.compose.SimpleTooltip
import kotlinx.coroutines.launch

@Composable
fun InstallUEPakModScreen(
    screenState: InstallUEPakModScreenState
) {
    val snackbarState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val isScrolling by remember { derivedStateOf { scrollState.value > 0 } }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Material3Theme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                HeightSpacer(16.dp)
                TopBar(isScrolling, screenState.canExitScreen, screenState::exitScreen)

                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        HeightSpacer(16.dp)
                        Text(
                            text = "Installation",
                            style = Material3Theme.typography.titleLarge,
                            color = Material3Theme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        HeightSpacer(8.dp)
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            val str = buildAnnotatedString {
                                append("1. Import the mod archives below")

                                append("\n\n")
                                withStyle(Material3Theme.typography.bodySmall.toSpanStyle()) {
                                    append("*note: previous installation will be deleted")
                                }
                            }
                            val uriHandler = LocalUriHandler.current
                            ClickableText(
                                text = str,
                                style = Material3Theme.typography.bodyLarge.copy(color = Material3Theme.colorScheme.onSurface),
                                onClick = { offset ->
                                    str.getStringAnnotations(tag = "ue4ss", start = offset, end = offset).let { link ->
                                        if (link.isNotEmpty())
                                            uriHandler.openUri(link.first().item)
                                    }
                                }
                            )

                            HeightSpacer(32.dp)
                            SelectUEPakArchiveUICard(screenState, snackbarState, Modifier)
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
                            val absOnSurface = Material3Theme.absoluteContentColor
                            defaultScrollbarStyle().copy(
                                unhoverColor = absOnSurface.copy(alpha = 0.25f),
                                hoverColor = absOnSurface.copy(alpha = 0.50f),
                                thickness = 4.dp
                            )
                        }
                    )
                }
            }
            SnackbarHost(
                snackbarState,
                Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun TopBar(
    isScrolling: Boolean = false,
    canGoBack: Boolean = false,
    goBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Material3Theme.surfaceColorAtElevation(
                    surface = Material3Theme.colorScheme.surface,
                    elevation = if (isScrolling) 2.dp else 0.dp,
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
                        Box (
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable(enabled = canGoBack, onClick = goBack)
                                .padding(2.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource("drawable/arrow_left_simple_32px.png"),
                                tint = Material3Theme.colorScheme.onSurface.copy(alpha = if (canGoBack) 1f else 0.1f),
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
                    "Install Unreal Engine Pak Mod",
                    style = Material3Theme.typography.headlineMedium,
                    color = Material3Theme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SelectUEPakArchiveUICard(
    state: InstallUEPakModScreenState,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
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
                if (state.isLoading) {
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
                            text = state.statusMessage ?: "",
                            style = Material3Theme.typography.titleMedium,
                            color = Material3Theme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                } else if (/*state.selectedUEPakArchive == null*/ true) {
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
                                state.dragAndDropUEPakModArchives(
                                    (event.dragData() as DragData.FilesList).readFiles()
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
                                            files.size <= 256 && files.first().let { uriStr ->
                                                uriStr.startsWith("file:/") &&
                                                    state.fs.file(Path.of(java.net.URI(uriStr))).isRegularFile() &&
                                                    (
                                                        uriStr.endsWith(".zip", ignoreCase = true) ||
                                                        uriStr.endsWith(".rar", ignoreCase = true) ||
                                                        uriStr.endsWith(".7z", ignoreCase = true)
                                                    )
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
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            )
                            .clickable { state.pickUEPakModArchives(null) }
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
                            text = "Select or Drop the archive here",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyMedium
                        )

                        if (state.isError) {
                            HeightSpacer(16.dp)
                            val ins = remember { MutableInteractionSource() }
                            val clipBoardManager = LocalClipboardManager.current
                            val text = buildAnnotatedString {
                                append("[Error]: ${state.statusMessage}")
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
                        } else if (state.isInstalledSuccessfully) {
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