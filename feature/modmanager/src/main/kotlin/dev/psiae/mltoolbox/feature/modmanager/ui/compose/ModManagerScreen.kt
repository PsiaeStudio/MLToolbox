 package dev.psiae.mltoolbox.feature.modmanager.ui.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.modmanager.ui.browsemods.BrowseModsScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset.ManagePlaysetScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.setup.SetupModManagerScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.dashboard.DashboardScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.launcher.LauncherScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.ManageModsScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.manageplayset.ManagePlaysetScreen
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.graphics.painter.NoopPainter
import dev.psiae.mltoolbox.foundation.ui.compose.inert
import dev.psiae.mltoolbox.foundation.ui.compose.md3.currentOrThrow
import dev.psiae.mltoolbox.foundation.ui.compose.md3.rippleAlphaOrDefault
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf

@Composable
fun ModManagerScreen() {
    val screenModel = rememberModManagerScreenModel()
    val screenState = rememberModManagerScreenState(screenModel)
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Material3Theme.colorScheme.surface
    ) {
        if (!screenState.isReady)
            return@Surface
        if (screenState.isNeedSetup) {
            SetupModManagerScreen(screenState)
            return@Surface
        }
        Row {
            NavigationDrawer(screenState)
            Box {
                screenState.navigator.stack.forEachIndexed { i, name ->
                    key(name) {
                        Box(
                            Modifier
                                .thenIf(i < screenState.navigator.stack.lastIndex) {
                                    Modifier
                                        .inert()
                                }
                        ) {
                            when (name) {
                                DashboardScreen.name -> DashboardScreen(screenState)
                                LauncherScreen.name -> LauncherScreen(screenState)
                                ManageModsScreen.name -> ManageModsScreen(screenState)
                                ManagePlaysetScreen.name -> ManagePlaysetScreen(screenState)
                                else -> WIPScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationDrawer(
    modManagerScreenState: ModManagerScreenState,
) {
    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp),
        color = Material3Theme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .width(216.dp)
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
                NavigationDrawerItem(
                    isSelected = modManagerScreenState
                        .currentDrawerDestination == DashboardScreen.name,
                    displayName = "Dashboard",
                    enabled = true,
                    icon = painterResource("drawable/icon_dashboard_outlined_24px.png"),
                    onClick = modManagerScreenState.navigator::navigateToDashboard
                )
                NavigationDrawerItem(
                    isSelected = modManagerScreenState
                        .currentDrawerDestination == LauncherScreen.name,
                    displayName = "Launcher",
                    enabled = true,
                    icon = NoopPainter,
                    onClick = modManagerScreenState.navigator::navigateToLauncher
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
                NavigationDrawerItem(
                    isSelected = modManagerScreenState
                        .currentDrawerDestination == BrowseModsScreen.name,
                    displayName = "Browse Mods",
                    enabled = false,
                    icon = painterResource("drawable/icon_browse_internet_outline_outlined_24px.png"),
                    onClick = modManagerScreenState.navigator::navigateToBrowseMods
                )
                NavigationDrawerItem(
                    isSelected = modManagerScreenState
                        .currentDrawerDestination == ManageModsScreen.name,
                    displayName = "Manage Mods",
                    enabled = true,
                    icon = NoopPainter,
                    onClick = modManagerScreenState.navigator::navigateToManageMods
                )
                NavigationDrawerItem(
                    isSelected = modManagerScreenState
                        .currentDrawerDestination == ManagePlaysetScreen.name,
                    displayName = "Manage Playset",
                    enabled = true,
                    icon = NoopPainter,
                    onClick = modManagerScreenState.navigator::navigateToManagePlayset
                )
                /*SimpleTooltip(
                    "Interact with modded game instances (WIP)"
                ) {
                    NavigationDrawerItem(
                        isSelected = modManagerScreenModel
                            .currentDrawerDestination == "mods_runtime",
                        displayName = "Runtime (WIP)",
                        enabled = false,
                        icon = NoopPainter,
                        onClick = {  }
                    )
                }*/
            }
            /*if (scrollState.canScrollForward or scrollState.canScrollBackward) {
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
            }*/
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawerItem(
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
            .clickable(enabled = enabled, interactionSource = ins, onClick = onClick)
            .composed {
                val rippleTheme = LocalRippleConfiguration.currentOrThrow()
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
            if (icon != NoopPainter) {
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
private fun WIPScreen(
    background: Color = Material3Theme.colorScheme.surface
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(Unit) {}
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