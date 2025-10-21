package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.browse.BrowseScreen
import dev.psiae.mltoolbox.feature.modmanager.dashboard.ui.DashboardScreen
import dev.psiae.mltoolbox.feature.modmanager.launcher.ui.LauncherScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.ManageModsScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.manageplayset.ManagePlaysetScreen
import dev.psiae.mltoolbox.shared.ui.composeui.HeightSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.LocalApplication
import dev.psiae.mltoolbox.shared.ui.composeui.NoOpPainter
import dev.psiae.mltoolbox.shared.ui.composeui.WidthSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.focusGate
import dev.psiae.mltoolbox.shared.ui.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.composeui.md3.requireCurrent
import dev.psiae.mltoolbox.shared.ui.composeui.md3.rippleAlphaOrDefault
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.ripple
import dev.psiae.mltoolbox.shared.ui.md3.MD3Spec
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import dev.psiae.mltoolbox.shared.ui.md3.incrementsDp
import dev.psiae.mltoolbox.shared.ui.md3.padding


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModManagerMainScreen() {
    val app = LocalApplication.current
    val modManager = rememberModManager(app.modManager)
    val modManagerScreen = rememberModManagerScreenState(modManager)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        CompositionLocalProvider(
            LocalIndication provides MD3Theme.ripple(),
        ) {
            if (!modManagerScreen.hasGameWorkingDirectory) {
                SelectGameWorkingDirectoryScreen(modManagerScreen)
                return@CompositionLocalProvider
            }
            Row {
                NavigationDrawer(modManagerScreen)
                Box {
                    Box(
                        modifier = Modifier.zIndex(if (modManagerScreen.currentDrawerDestination == "dashboard") 1f else 0f)
                            .focusGate(modManagerScreen.currentDrawerDestination == "dashboard")
                    ) {
                        DashboardScreen(modManagerScreen)
                    }
                    Box(
                        modifier = Modifier.zIndex(if (modManagerScreen.currentDrawerDestination == "launcher") 1f else 0f)
                            .focusGate(modManagerScreen.currentDrawerDestination == "launcher")
                    ) {
                        LauncherScreen(modManagerScreen)
                    }
                    Box(
                        modifier = Modifier.zIndex(if (modManagerScreen.currentDrawerDestination == "browse_mods") 1f else 0f)
                            .focusGate(modManagerScreen.currentDrawerDestination == "browse_mods")
                    ) {
                        BrowseScreen(modManagerScreen)
                    }
                    Box(
                        modifier = Modifier.zIndex(if (modManagerScreen.currentDrawerDestination == "manage_mods") 1f else 0f)
                            .focusGate(modManagerScreen.currentDrawerDestination == "manage_mods")
                    ) {
                        ManageModsScreen(modManagerScreen)
                    }
                    Box(
                        modifier = Modifier.zIndex(if (modManagerScreen.currentDrawerDestination == "manage_playset") 1f else 0f)
                            .focusGate(modManagerScreen.currentDrawerDestination == "manage_mods")
                    ) {
                        ManagePlaysetScreen(modManagerScreen)
                    }
                    Box(
                        modifier = Modifier.zIndex(0.5f)
                    ) {
                        WIPScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun UE4SSNotInstalledUI(
    modManagerScreenState: ModManagerScreenState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        BoxWithConstraints(modifier = Modifier
            .padding(8.dp)
            .defaultMinSize(minWidth = 1200.dp)
            .defaultMinSize(minHeight = 36.dp)
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (LocalIsDarkTheme.current)
                    Modifier.shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
                else
                    Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
            )
            .clickable { modManagerScreenState.userInputChangeWorkingDir() }
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
                    val f = modManagerScreenState.gameBinaryFile
                    val fp = modManagerScreenState.gameBinaryFile?.absolutePath
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberScrollState()
            Row {
                Column(
                    modifier = Modifier
                        .weight(1f, false)
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .defaultMinSize(minWidth = 400.dp)
                            .padding(horizontal = 48.dp)
                            .align(Alignment.CenterHorizontally),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF46492f), contentColor = Color(0xFF2c0b12))
                    ) {
                        Column(
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "UE4SS is not installed",
                                style = Material3Theme.typography.titleLarge,
                                color = Color(0xFFffb4ab),
                                maxLines = 1
                            )
                            HeightSpacer(8.dp)
                            Row {
                                Text(
                                    modifier = Modifier,
                                    text = "[message]: ",
                                    style = Material3Theme.typography.bodyMedium,
                                    color = Color(0xFFffb4ab),
                                    maxLines = 1
                                )
                                Text(
                                    modifier = Modifier,
                                    text = (modManagerScreenState.ue4ssNotInstalledMessage ?: "no_message_provided"),
                                    style = Material3Theme.typography.bodyMedium,
                                    color = Color(0xFFffb4ab),
                                    maxLines = 1
                                )
                            }
                            HeightSpacer(36.dp)
                            Row(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(Color(0xFFc2cd7c))
                                        .clickable { modManagerScreenState.userInputRetryCheckUE4SSInstalled() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource("drawable/icon_reload_24px.png"),
                                        contentDescription = null,
                                        tint = Color(0xFF2d3400)
                                    )
                                    WidthSpacer(8.dp)
                                    Text(
                                        modifier = Modifier.weight(1f, false),
                                        text = "RETRY",
                                        style = Material3Theme.typography.labelLarge.copy(
                                            baselineShift = BaselineShift(-0.1f)
                                        ),
                                        color = Color(0xFF2d3400),
                                        maxLines = 1
                                    )
                                }
                                WidthSpacer(16.dp)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(Color(0xFFc2cd7c))
                                        .clickable { modManagerScreenState.userInputInstallUE4SS() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f, false),
                                        text = "INSTALL UE4SS",
                                        style = Material3Theme.typography.labelLarge.copy(
                                            baselineShift = BaselineShift(-0.1f)
                                        ),
                                        color = Color(0xFF2d3400),
                                        maxLines = 1
                                    )
                                    WidthSpacer(12.dp)
                                    Icon(
                                        painter = painterResource("drawable/icon_arrow_right_24px.png"),
                                        contentDescription = null,
                                        tint = Color(0xFF2d3400)
                                    )
                                }
                            }
                        }
                    }
                    HeightSpacer(24.dp)
                }
                VerticalScrollbar(
                    modifier = Modifier
                        .height(
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
                    }
                )
            }
        }
    }
}

@Composable
private fun NavigationDrawer(
    modManagerScreenState: ModManagerScreenState,
) {
    Row(
        modifier = Modifier
            .width(216.dp)
            .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp))
            .background(Material3Theme.colorScheme.surfaceContainerLow)
    ) {
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = Material3Theme.colorScheme.primary
        )
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "General",
                    style = Material3Theme.typography.titleSmall,
                    color = Material3Theme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            NavigationDrawerItemUI(
                isSelected = modManagerScreenState
                    .currentDrawerDestination == "dashboard",
                displayName = "Dashboard",
                enabled = true,
                icon = painterResource("drawable/icon_dashboard_outlined_24px.png"),
                onClick = { modManagerScreenState.currentDrawerDestination = "dashboard" }
            )
            NavigationDrawerItemUI(
                isSelected = modManagerScreenState
                    .currentDrawerDestination == "launcher",
                displayName = "Launcher",
                enabled = true,
                icon = NoOpPainter,
                onClick = { modManagerScreenState.currentDrawerDestination = "launcher" }
            )
            Box(
                modifier = Modifier
                    .height(12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.align(Alignment.Center).width(216.dp).padding(horizontal = 8.dp))
            }
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Mods",
                    style = Material3Theme.typography.titleSmall,
                    color = Material3Theme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            NavigationDrawerItemUI(
                isSelected = modManagerScreenState
                    .currentDrawerDestination == "browse_mods",
                displayName = "Browse Mods",
                enabled = true,
                icon = painterResource("drawable/icon_browse_internet_outline_outlined_24px.png"),
                onClick = { modManagerScreenState.currentDrawerDestination = "browse_mods" }
            )
            NavigationDrawerItemUI(
                isSelected = modManagerScreenState
                    .currentDrawerDestination == "manage_mods",
                displayName = "Manage Mods",
                enabled = true,
                icon = NoOpPainter,
                onClick = { modManagerScreenState.currentDrawerDestination = "manage_mods" }
            )
            NavigationDrawerItemUI(
                isSelected = modManagerScreenState
                    .currentDrawerDestination == "manage_playset",
                displayName = "Manage Playset",
                enabled = true,
                icon = NoOpPainter,
                onClick = { modManagerScreenState.currentDrawerDestination = "manage_playset" }
            )
            SimpleTooltip(
                "Interact with modded game instances (WIP)"
            ) {
                NavigationDrawerItemUI(
                    isSelected = modManagerScreenState
                        .currentDrawerDestination == "mods_runtime",
                    displayName = "Runtime (WIP)",
                    enabled = false,
                    icon = NoOpPainter,
                    onClick = { modManagerScreenState.currentDrawerDestination = "mods_runtime" }
                )
            }
        }
        if (scrollState.canScrollForward or scrollState.canScrollBackward) {
            VerticalScrollbar(
                modifier = Modifier
                    .height(
                        with(LocalDensity.current) {
                            remember(this) {
                                derivedStateOf { scrollState.viewportSize.toDp() }
                            }.value
                        }
                    )
                    .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp))
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
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawerItemUI(
    isSelected: Boolean,
    displayName: String,
    enabled: Boolean,
    icon: Painter,
    onClick: () -> Unit
) {
    val ins = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .height(48.dp)
            .width((216).dp)
            .clip(RoundedCornerShape(50.dp))
            .clickable(enabled = enabled, indication = MD3Theme.ripple(), interactionSource = ins, onClick = onClick)
            .composed {
                val rippleTheme = LocalRippleConfiguration.requireCurrent()
                if (ins.collectIsHoveredAsState().value) {
                    Modifier
                        .background(color = rippleTheme.color.copy(alpha = rippleTheme.rippleAlphaOrDefault().hoveredAlpha))
                } else {
                    Modifier
                }
            },
    )  {
        val animatedWidth = animateFloatAsState(
            if (isSelected) 1f else 0.5f,
            animationSpec = tween(100)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(48.dp)
                    .width((animatedWidth.value * 216).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Material3Theme.colorScheme.secondaryContainer)
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != NoOpPainter) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(if (enabled) 1f else 0.38f),
                    painter = icon,
                    contentDescription = null,
                    tint = if (isSelected)
                        Material3Theme.colorScheme.onSecondaryContainer
                    else
                        Material3Theme.colorScheme.onSurfaceVariant
                )
            } else {
                WidthSpacer(24.dp)
            }
            WidthSpacer(12.dp)
            Text(
                modifier = Modifier.alpha(if (enabled) 1f else 0.38f),
                text = displayName,
                style = Material3Theme.typography.labelLarge,
                color = if (isSelected)
                    Material3Theme.colorScheme.onSecondaryContainer
                else
                    Material3Theme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun WIPScreen(
    background: Color = Material3Theme.colorScheme.surface
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .defaultSurfaceGestureModifiers()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "WIP",
            style = Material3Theme.typography.displayLarge,
            color = Material3Theme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )
    }
}