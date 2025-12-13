package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.launcher.direct

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.SimpleTooltip
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.WIPScreen
import dev.psiae.mltoolbox.feature.modmanager.launcher.ui.LauncherScreenState
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.graphics.painter.NoopPainter
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.shared.ui.compose.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.compose.md3.requireCurrent
import dev.psiae.mltoolbox.shared.ui.compose.md3.rippleAlphaOrDefault
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.compose.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.shared.ui.md3.MD3Spec
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import dev.psiae.mltoolbox.shared.ui.md3.incrementsDp
import dev.psiae.mltoolbox.shared.ui.md3.padding

@Composable
fun DirectLauncherScreen(
    launcherScreenState: LauncherScreenState
) {
    val state = rememberDirectLauncherScreenState(launcherScreenState)
    val snackbar = remember(state) {
        SnackbarHostState()
    }.also { snackbar ->
        LaunchedEffect(snackbar) {
            for (errMsg in state.snackbarErrorChannel) {
                snackbar.showSnackbar(message = errMsg, withDismissAction = true, duration = SnackbarDuration.Indefinite)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            HeightSpacer(16.dp)
            Box {
                SelectedWorkingBinaryFilePanel(launcherScreenState)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .border(
                        width = 1.dp,
                        color = Material3Theme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                    .defaultSurfaceGestureModifiers(),
            ) {
                var width by remember {
                    mutableStateOf(0.dp)
                }
                val density = LocalDensity.current
                Column(
                    // TODO: measure content ourselves then render the divider based on it
                    modifier = Modifier
                        .onGloballyPositioned { coord ->
                            with(density) { coord.size.width.toDp() }.let {
                                width = it
                            }
                        }
                ) {
                    HeightSpacer(16.dp)
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        ModeNavigation(state)
                    }
                    HeightSpacer(16.dp)
                    HorizontalDivider(
                        modifier = Modifier.width(width),
                        thickness = 1.dp,
                        color = Material3Theme.colorScheme.outlineVariant
                    )
                    HeightSpacer(8.dp)
                    Box {
                        Box(
                            modifier = Modifier.zIndex(if (state.selectedTab == "direct") 1f else 0f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Material3Theme.colorScheme.surfaceContainer)
                                    .defaultSurfaceGestureModifiers()
                            ) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Row {
                                        Row(
                                            modifier = Modifier
                                                .height(40.dp)
                                                .defaultMinSize(minWidth = 120.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Material3Theme.colorScheme.secondaryContainer)
                                                .clickable { state.userInputLaunchGame() }
                                                .padding(vertical = 6.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            if (state.launchingModded) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier
                                                        .size((18+4).dp),
                                                    strokeWidth = 1.dp
                                                )
                                            } else {
                                                Icon(
                                                    modifier = Modifier
                                                        .shadow(2.dp, RoundedCornerShape(50))
                                                        .size((18+4).dp)
                                                        .clip(RoundedCornerShape(50)),
                                                    painter = painterResource("drawable/1.ico"),
                                                    contentDescription = null,
                                                    tint = Color.Unspecified
                                                )
                                            }
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
                                        WidthSpacer(12.dp)
                                        run {
                                            val enabled = true
                                            Row(
                                                modifier = Modifier
                                                    .height(40.dp)
                                                    .defaultMinSize(minWidth = 120.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(if (enabled) Material3Theme.colorScheme.secondaryContainer else Material3Theme.colorScheme.onSurface.copy(alpha = 0.12f))
                                                    .clickable(enabled = enabled) { state.userInputLaunchVanilla() }
                                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                if (state.launchingVanilla) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier
                                                            .size((18+4).dp),
                                                        strokeWidth = 1.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        modifier = Modifier
                                                            .shadow(2.dp, RoundedCornerShape(50))
                                                            .size((18+4).dp)
                                                            .clip(RoundedCornerShape(50)),
                                                        painter = painterResource("drawable/1.ico"),
                                                        contentDescription = null,
                                                        tint = Color.Unspecified
                                                    )
                                                }
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
                                    }

                                }
                            }
                        }
                        Box(
                            modifier = Modifier.zIndex(if (state.selectedTab == "managed") 1f else 0f)
                        ) {
                            WIPScreen(background = Material3Theme.colorScheme.surfaceContainer)
                        }
                    }
                }
            }
        }
        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            hostState = snackbar
        )
    }
}

@Composable
private fun ModeNavigation(
    screenState: DirectLauncherScreenState
) {
    val scrollState = rememberScrollState()
    val contentIns = remember { MutableInteractionSource() }
    val scrollBarIns = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(contentIns)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Start)
                .horizontalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                    .border(1.dp, Material3Theme.colorScheme.outline, RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier/*.weight(1f, false)*/) {
                    ModeNavigationItemUI(
                        displayName = "Direct Playset",
                        isSelected = screenState.selectedTab == "direct",
                        width = 220.dp,
                        enabled = true,
                        icon = NoopPainter,
                        tintIcon = true,
                        tooltipDescription = "Playset installed directly in the game install directory",
                        onClick = { screenState.selectedTab = "direct"  }
                    )
                }
                Box(modifier = Modifier/*.weight(1f, false)*/) {
                    ModeNavigationItemUI(
                        displayName = "Managed Playset (WIP)",
                        isSelected = screenState.selectedTab == "managed",
                        width = 220.dp,
                        enabled = false,
                        icon = NoopPainter,
                        tintIcon = true,
                        tooltipDescription = "Playset managed in this app ",
                        onClick = { screenState.selectedTab = "managed"  }
                    )
                }
            }
        }
        if (
            run {
                // remove true to only start show when interacted
                true ||
                        contentIns.collectIsHoveredAsState().value or
                        scrollBarIns.collectIsDraggedAsState().value
            } && scrollState.canScrollForward or scrollState.canScrollBackward
        ) {
            HeightSpacer(2.dp)
            HorizontalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(
                        with(LocalDensity.current) {
                            remember(this) {
                                derivedStateOf { scrollState.viewportSize.toDp() }
                            }.value
                        }
                    )
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .then(
                        if (scrollState.canScrollForward or scrollState.canScrollBackward)
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
                },
                interactionSource = scrollBarIns
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeNavigationItemUI(
    displayName: String,
    width: Dp,
    isSelected: Boolean,
    enabled: Boolean,
    icon: Painter = NoopPainter,
    tintIcon: Boolean = true,
    tooltipDescription: String?,
    onClick: () -> Unit,
) {
    val ins = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .width(width)
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, indication = null, interactionSource = ins, onClick = onClick)
            .composed {
                val rippleTheme = LocalRippleConfiguration.requireCurrent()
                if (ins.collectIsHoveredAsState().value) {
                    Modifier
                        .background(color = rippleTheme.color.copy(alpha = rippleTheme.rippleAlphaOrDefault().hoveredAlpha))
                } else {
                    Modifier
                }
            }
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(36.dp)
                    .width((1 * width.value).dp)
                    .background(Material3Theme.colorScheme.secondaryContainer)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != NoopPainter) {
                Box {
                    /*if (isSelected && !tintIcon) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Material3Theme.colorScheme.onSecondaryContainer)
                        ) {

                        }
                    }*/
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                            .alpha(if (enabled) 1f else 0.38f),
                        painter = icon,
                        contentDescription = null,
                        tint = if (!tintIcon)
                            Color.Unspecified
                        else if (isSelected)
                            Material3Theme.colorScheme.onSecondaryContainer
                        else Material3Theme.colorScheme.onSurfaceVariant
                    )
                }
                WidthSpacer(8.dp)
            }
            Text(
                modifier = Modifier.alpha(if (enabled) 1f else 0.38f),
                text = displayName,
                style = Material3Theme.typography.labelLarge.copy(
                    baselineShift = BaselineShift(-0.1f),
                ),
                maxLines = 1,
                color = Material3Theme.colorScheme.onSecondaryContainer,
            )
            tooltipDescription?.let {
                WidthSpacer(12.dp)
                SimpleTooltip(
                    tooltipDescription
                ) {
                    Icon(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically),
                        painter = painterResource("drawable/icon_info_filled_32px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedWorkingBinaryFilePanel(
    launcherScreenState: LauncherScreenState
) {
    BoxWithConstraints(modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .defaultMinSize(minWidth = 1200.dp)
        .defaultMinSize(minHeight = 36.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .then(
            if (LocalIsDarkTheme.current)
                Modifier.shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
            else
                Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
        )
        .clickable { launcherScreenState.modManagerScreenState.userInputChangeWorkingDir() }
        .background(Material3Theme.colorScheme.inverseOnSurface)
        .padding(MD3Spec.padding.incrementsDp(2).dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 2.dp)
                .defaultMinSize(minWidth = with(LocalDensity.current) { constraints.minWidth.toDp() }),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            run {
                val f = launcherScreenState.modManagerScreenState.gameBinaryFile
                val fp = launcherScreenState.modManagerScreenState.gameBinaryFile?.absolutePath
                val (fp1, fp2) = remember(f) {
                    run {
                        var dash = false
                        fp?.dropLastWhile { c -> !dash.also { dash = c == '\\' } }
                    } to run {
                        var dash = false
                        fp?.takeLastWhile { c -> !dash.also { dash = c == '\\' } }
                    }
                }
                val color = Material3Theme.colorScheme.onSurface.copy(alpha = 0.78f)
                Text(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .align(Alignment.CenterVertically),
                    text = fp1?.plus(fp2) ?: "Click here to select game executable",
                    style = Material3Theme.typography.labelMedium,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
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
}