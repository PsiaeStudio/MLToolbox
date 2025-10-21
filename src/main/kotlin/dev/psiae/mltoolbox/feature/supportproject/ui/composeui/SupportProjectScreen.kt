package dev.psiae.mltoolbox.feature.supportproject.ui.composeui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.shared.ui.composeui.HeightSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.ripple
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun DonateMainScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surface)
            .defaultSurfaceGestureModifiers()
    ) {
        CompositionLocalProvider(
            LocalIndication provides MD3Theme.ripple(),
        ) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .border(
                        width = 1.dp,
                        color = Material3Theme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                    .padding(32.dp),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PsiaeCardUI(modifier = Modifier)
                    PatreonCardUI(modifier = Modifier)
                }

                HeightSpacer(36.dp)
                Text(
                    text = buildAnnotatedString {
                        append("Consider supporting us to keep the app working, up to date, and free to use for all user")
                    },
                    color = Material3Theme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun PsiaeCardUI(
    modifier: Modifier
) {
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier) {
        ElevatedCard(
            modifier = Modifier
                .then(
                    if (LocalIsDarkTheme.current)
                        Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                    else
                        Modifier
                )
            /*.verticalScroll(rememberScrollState())*/,
            colors = CardDefaults.cardColors(containerColor = Material3Theme.colorScheme.surfaceContainerHigh, contentColor = Material3Theme.colorScheme.onSurface),
        ) {
            Column(
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch(Dispatchers.IO) {
                            uriHandler.openUri("https://www.psiae.fun/")
                        }
                    }
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .padding(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "æ",
                            color = Color.White,
                            style = MaterialTheme.typography.displayLarge,
                        )
                    }
                }
                HeightSpacer(36.dp)
                Text(
                    modifier = Modifier,
                    text = "Psiae",
                    color = Material3Theme.colorScheme.onSurface,
                    style = Material3Theme.typography.titleMedium
                )
            }

        }
    }
}
@Composable
private fun PatreonCardUI(
    modifier: Modifier
) {
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier) {
        ElevatedCard(
            modifier = Modifier
                .then(
                    if (LocalIsDarkTheme.current)
                        Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                    else
                        Modifier
                )
            /*.verticalScroll(rememberScrollState())*/,
            colors = CardDefaults.cardColors(containerColor = Material3Theme.colorScheme.surfaceContainerHigh, contentColor = Material3Theme.colorScheme.onSurface),
        ) {
            Column(
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch(Dispatchers.IO) {
                            uriHandler.openUri("https://www.patreon.com/c/psiae/membership")
                        }
                    }
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .padding(36.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(72.dp).align(Alignment.Center),
                        painter = painterResource("drawable/PATREON_SYMBOL_1_WHITE_RGB.svg"),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                HeightSpacer(36.dp)
                Text(
                    modifier = Modifier,
                    text = "Patreon",
                    color = Material3Theme.colorScheme.onSurface,
                    style = Material3Theme.typography.titleMedium
                )
            }

        }
    }
}

@Composable
private fun TwitchCardUI(
    modifier: Modifier
) {
    val uriHandler = LocalUriHandler.current
    BoxWithConstraints(modifier) {
        ElevatedCard(
            modifier = Modifier
                .clickable { uriHandler.openUri("https://www.twitch.tv/psiae1") }
            /*.verticalScroll(rememberScrollState())*/,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF46492f), contentColor = Color(0xFF2c0b12)),
        ) {
            Column(
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(240, 240, 255))
                        .padding(36.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(72.dp).align(Alignment.Center),
                        painter = painterResource("drawable/glitch_flat_purple_convert_72px.png"),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
                HeightSpacer(36.dp)
                Text(
                    modifier = Modifier,
                    text = "Twitch",
                    color = Color(0xFFe5e5c0),
                    style = Material3Theme.typography.titleMedium
                )
            }

        }
    }
}