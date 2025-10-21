package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.SimpleTooltip
import dev.psiae.mltoolbox.shared.ui.composeui.HeightSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.WidthSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.ripple
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.surfaceColorAtElevation
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DirectInstallModScreen(
    manageDirectModsScreenState: ManageDirectModsScreenState
) {
    val state = rememberDirectInstallModScreenState(manageDirectModsScreenState)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        Box {
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
                            .height(64.dp)
                            .fillMaxWidth()
                            .background(
                                color = MD3Theme.surfaceColorAtElevation(
                                    surface = Material3Theme.colorScheme.surface,
                                    elevation = if (topBarScrolling) 2.dp else 0.dp,
                                    tint = Material3Theme.colorScheme.primary
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SimpleTooltip(
                                "go back"
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clickable(onClick = { state.userInputExit() })
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource("drawable/arrow_left_simple_32px.png"),
                                        tint = Material3Theme.colorScheme.onSurface,
                                        contentDescription = null
                                    )
                                }
                            }

                            WidthSpacer(16.dp)

                            Row {
                                Text(
                                    "Install mod",
                                    style = Material3Theme.typography.headlineSmall.copy(
                                        baselineShift = BaselineShift(-0.1f)
                                    ),
                                    color = Material3Theme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Row {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .verticalScroll(state = scrollState)
                        ) {
                            Text(
                                "Supported mod loaders",
                                style = Material3Theme.typography.titleLarge,
                                color = Material3Theme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            HeightSpacer(12.dp)
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                REUE4SSModLoaderCard(state)
                                HeightSpacer(16.dp)
                                UnrealEngineModLoaderCard(state)
                            }
                            HeightSpacer(24.dp)
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
        if (state.navigateToInstallUE4SS) {
            DirectInstallUE4SSScreen(state)
        }
        if (state.navigateToInstallUE4SSMod) {
            DirectInstallUE4SSModScreen(state)
        }
        if (state.navigateToInstallUnrealEngineMod) {
            DirectInstallUEPakModScreen(state)
        }
    }
}

@Composable
private fun REUE4SSModLoaderCard(
    state: DirectInstallModScreenState
) {
    Column(
        modifier = Modifier
            .heightIn(max = 270.dp)
            .width(480.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (LocalIsDarkTheme.current)
                    Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                else
                    Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            )
            .background(Material3Theme.colorScheme.surfaceContainerHigh)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f, false)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RE-UE4SS",
                    style = Material3Theme.typography.titleMedium.copy(
                        baselineShift = BaselineShift(-0.1f)
                    ),
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                run {
                    val uriHandler = LocalUriHandler.current
                    val coroutineScope = rememberCoroutineScope()
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { coroutineScope.launch(Dispatchers.IO) { uriHandler.openUri("https://github.com/UE4SS-RE/RE-UE4SS") } }
                            .padding(4.dp),
                        painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                        tint = Material3Theme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
            HeightSpacer(4.dp)
            HorizontalDivider(
                modifier = Modifier.width((480-48).dp),
                thickness = 1.dp,
                Material3Theme.colorScheme.outline
            )
            HeightSpacer(12.dp)
            Box {
                Text(
                    text = "Injectable LUA scripting system, SDK generator, live property editor and other dumping utilities for UE4/5 games",
                    style = Material3Theme.typography.bodySmall,
                    color = Material3Theme.colorScheme.onSurface,
                )
            }
        }
        HeightSpacer(16.dp)
        Row(
            modifier = Modifier.height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {

            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Transparent)
                        .clickable { state.userInputNavigateToInstallUE4SSMod() }
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Install Mod",
                        style = Material3Theme.typography.labelLarge.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = Material3Theme.colorScheme.primary,
                        maxLines = 1
                    )
                }
                WidthSpacer(8.dp)
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .defaultMinSize(minWidth = 120.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Material3Theme.colorScheme.primary)
                        .clickable { state.userInputNavigateToInstallUE4SS() }
                        .padding(vertical = 6.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Install Mod Loader",
                        style = Material3Theme.typography.labelLarge.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = Material3Theme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun UnrealEngineModLoaderCard(
    state: DirectInstallModScreenState
) {
    Column(
        modifier = Modifier
            .heightIn(max = 270.dp)
            .width(480.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (LocalIsDarkTheme.current)
                    Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                else
                    Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            )
            .background(Material3Theme.colorScheme.surfaceContainerHigh)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f, false)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Unreal Engine Pak Patcher",
                    style = Material3Theme.typography.titleMedium.copy(
                        baselineShift = BaselineShift(-0.1f)
                    ),
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                run {
                    val uriHandler = LocalUriHandler.current
                    val coroutineScope = rememberCoroutineScope()
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { coroutineScope.launch(Dispatchers.IO) { uriHandler.openUri("https://dev.epicgames.com/documentation/en-us/unreal-engine/patching-content-delivery-and-dlc-in-unreal-engine") } }
                            .padding(4.dp),
                        painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                        tint = Material3Theme.colorScheme.primary,
                        contentDescription = null
                    )
                }
                WidthSpacer(6.dp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Material3Theme.colorScheme.tertiary)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        "Built-in",
                        style = Material3Theme.typography.bodySmall.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = Material3Theme.colorScheme.onTertiary,
                        maxLines = 1
                    )
                }
            }
            HeightSpacer(4.dp)
            HorizontalDivider(
                modifier = Modifier.width((480-48).dp),
                thickness = 1.dp,
                Material3Theme.colorScheme.outline
            )
            HeightSpacer(12.dp)
            Box {
                Text(
                    text = "Unreal Engine built-in pak patcher",
                    style = Material3Theme.typography.bodySmall,
                    color = Material3Theme.colorScheme.onSurface,
                )
            }
        }
        HeightSpacer(16.dp)
        Row(
            modifier = Modifier.height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {

            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                run {
                    val enabled = true
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Transparent)
                            .clickable(enabled = enabled) { state.userInputNavigateToInstallUnrealEngineMod() }
                            .padding(vertical = 6.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Install Mod",
                            style = Material3Theme.typography.labelLarge.copy(
                                baselineShift = BaselineShift(-0.1f)
                            ),
                            color = Material3Theme.colorScheme.primary.copy(
                                alpha = if (enabled) 1f else 0.38f
                            ),
                            maxLines = 1
                        )
                    }
                }

                WidthSpacer(8.dp)
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .defaultMinSize(minWidth = 120.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Material3Theme.colorScheme.onSurface.copy(alpha = 0.16f))
                        .clickable(enabled = false) {  }
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Installed",
                        style = Material3Theme.typography.labelLarge.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = Material3Theme.colorScheme.onSurface.copy(alpha = 0.38f),
                        maxLines = 1
                    )
                }
            }
        }
    }

}