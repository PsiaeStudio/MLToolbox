package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallUEPakModScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallUe4ssModScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallUe4ssScreen
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.graphics.surfaceColorAtElevation
import dev.psiae.mltoolbox.foundation.ui.compose.inert
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf
import dev.psiae.mltoolbox.foundation.ui.nav.ScreenNavEntry
import dev.psiae.mltoolbox.shared.ui.compose.SimpleTooltip

@Composable
fun InstallModScreen(
    screenState: InstallModScreenState
) {
    Box(
        modifier = Modifier.thenIf(screenState.navigator.stack.isNotEmpty()) {
            Modifier.inert()
        }
    ) {
        MainUI(screenState)
    }
    screenState.navigator.stack.forEachIndexed { i, e ->
        key(e.id) {
            Box(
                modifier = Modifier.thenIf(i < screenState.navigator.stack.lastIndex) {
                    Modifier.inert()
                }
            ) {
                when (e.screen) {
                    InstallUe4ssScreen -> InstallUe4ssScreen(screenState, e)
                    InstallUe4ssModScreen -> InstallUe4ssModScreen(screenState, e)
                    InstallUEPakModScreen -> InstallUEPakModScreen(screenState, e)
                }
            }
        }
    }
}

@Composable
private fun InstallUe4ssScreen(
    screenState: InstallModScreenState,
    entry: ScreenNavEntry
) {
    val state = rememberInstallUe4ssScreenState(
        goBack = { screenState.goBackFromScreen(entry) },
        model = rememberInstallUe4ssScreenModel(screenState.gameContext) {}
    ) {

    }
    InstallUe4ssScreen(state)
}
@Composable
private fun InstallUe4ssModScreen(
    screenState: InstallModScreenState,
    entry: ScreenNavEntry
) {
    val state = rememberInstallUe4ssModScreenState(
        goBack = { screenState.goBackFromScreen(entry) },
        model = rememberInstallUe4ssModScreenModel(screenState.gameContext) {}
    ) {

    }
    InstallUe4ssModScreen(state)
}

@Composable
private fun InstallUEPakModScreen(
    screenState: InstallModScreenState,
    entry: ScreenNavEntry
) {
    val state = rememberInstallUEPakModScreenState(
        goBack = { screenState.goBackFromScreen(entry) },
        model = rememberInstallUEPakModScreenModel(screenState.gameContext) {}
    ) {

    }
    InstallUEPakModScreen(state)
}

@Composable
private fun MainUI(
    screenState: InstallModScreenState
) {
    if (!screenState.isReady)
        return
    val scrollState = rememberScrollState()
    val isScrolling by remember { derivedStateOf { scrollState.value > 0 } }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Material3Theme.colorScheme.surface,
    ) {
        Column {
            HeightSpacer(16.dp)
            TopBar(isScrolling, screenState::exitScreen)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(state = scrollState)
            ) {
                HeightSpacer(12.dp)
                Text(
                    "Supported Mod Loaders",
                    style = Material3Theme.typography.titleLarge,
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                HeightSpacer(16.dp)
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Ue4ssModLoaderCard(
                        isInstalled = screenState.isUe4ssInstalled,
                        update = screenState::installUe4ssModLoader,
                        install = screenState::installUe4ssModLoader,
                        installMod = screenState::installUe4ssModLoaderMod,
                    )
                    HeightSpacer(16.dp)
                    UePakModLoaderCard(screenState::installUe4PakModLoaderMod)
                }
                HeightSpacer(24.dp)
            }
        }
    }
}


@Composable
private fun TopBar(
    isScrolling: Boolean = false,
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
                                .clickable(onClick = goBack)
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
                    "Install Mod",
                    style = Material3Theme.typography.headlineMedium,
                    color = Material3Theme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun Ue4ssModLoaderCard(
    isInstalled: Boolean,
    update: () -> Unit,
    install: () -> Unit,
    installMod: () -> Unit,
) {
    Column(
        modifier = Modifier
            .heightIn(max = 270.dp)
            .width(480.dp)
            .clip(RoundedCornerShape(12.dp))
            /*.then(
                if (LocalIsDarkTheme.current)
                    Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                else
                    Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            )*/
            .background(Material3Theme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f, false)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text(
                        "MLUE4SS",
                        style = Material3Theme.typography.titleMedium,
                        color = Material3Theme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    run {
                        val uriHandler = LocalUriHandler.current
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    // Change to https://psiae.fun when mod-page is ready, we only have chBeta there
                                    uriHandler.openUri("https://www.nexusmods.com/manorlords/mods/229")
                                }
                                .padding(4.dp),
                            painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                            tint = Material3Theme.colorScheme.primary,
                            contentDescription = null
                        )
                    }
                }
                Text(
                    "|",
                    style = Material3Theme.typography.titleMedium.copy(baselineShift = BaselineShift(-0.1f)),
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 3.dp)
                )
                WidthSpacer(4.dp)
                Row {
                    Text(
                        "RE-UE4SS",
                        style = Material3Theme.typography.titleMedium,
                        color = Material3Theme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    run {
                        val uriHandler = LocalUriHandler.current
                        val coroutineScope = rememberCoroutineScope()
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    uriHandler.openUri("https://github.com/UE4SS-RE/RE-UE4SS")
                                }
                                .padding(4.dp),
                            painter = painterResource("drawable/icon_navigate_redirect_24px.png"),
                            tint = Material3Theme.colorScheme.primary,
                            contentDescription = null
                        )
                    }
                }
            }
            HeightSpacer(8.dp)
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isInstalled) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable(onClick = installMod)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Install Mod",
                            style = Material3Theme.typography.labelLarge,
                            color = Material3Theme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    WidthSpacer(8.dp)
                }

                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable {
                            if (isInstalled) {
                                update()
                            } else {
                                install()
                            }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isInstalled)
                            "Update"
                        else
                            "Install",
                        style = Material3Theme.typography.labelLarge,
                        color = Material3Theme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun UePakModLoaderCard(
    installMod: () -> Unit,
) {
    Column(
        modifier = Modifier
            .heightIn(max = 270.dp)
            .width(480.dp)
            .clip(RoundedCornerShape(12.dp))
            /*.then(
                if (LocalIsDarkTheme.current)
                    Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                else
                    Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            )*/
            .background(Material3Theme.colorScheme.surfaceContainerLow)
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
                    style = Material3Theme.typography.titleMedium,
                    color = Material3Theme.colorScheme.onSurface,
                    maxLines = 1
                )
                run {
                    val uriHandler = LocalUriHandler.current
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                uriHandler.openUri("https://dev.epicgames.com/documentation/en-us/unreal-engine/patching-content-delivery-and-dlc-in-unreal-engine")
                            }
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
                        style = Material3Theme.typography.bodySmall,
                        color = Material3Theme.colorScheme.onTertiary,
                        maxLines = 1
                    )
                }
            }
            HeightSpacer(8.dp)
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
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable(enabled = enabled, onClick = installMod)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Install Mod",
                            style = Material3Theme.typography.labelLarge,
                            color = Material3Theme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                WidthSpacer(8.dp)
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .background(Material3Theme.colorScheme.onSurface.copy(alpha = 0.1f))
                        .clickable(enabled = false) {  }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Installed",
                        style = Material3Theme.typography.labelLarge,
                        color = Material3Theme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        maxLines = 1
                    )
                }
            }
        }
    }

}