package dev.psiae.mltoolbox.feature.setting.personalization.ui.composeui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.feature.setting.ui.SettingScreenState
import dev.psiae.mltoolbox.shared.ui.composeui.HeightSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.WidthSpacer
import dev.psiae.mltoolbox.shared.ui.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.ripple
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonalizationSettingScreen(
    state: SettingScreenState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animateColorAsState(
                if (LocalIsDarkTheme.current) Material3Theme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow,
                animationSpec = tween(200)
            ).value)
            .defaultSurfaceGestureModifiers()
    ) {
        CompositionLocalProvider(
            LocalIndication provides MD3Theme.ripple(),
        ) {
            PersonalizationSettingScreenContent(state)
        }
    }
}

@Composable
fun PersonalizationSettingScreenContent(
    state: SettingScreenState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeightSpacer(16.dp)
        Column(Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Settings",
                color = Material3Theme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = Material3Theme.typography.headlineLarge
            )
        }
        HeightSpacer(28.dp)
        Column(Modifier.padding(horizontal = 24.dp).width(650.dp)) {
            Text(
                text = "Theme",
                color = Material3Theme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = Material3Theme.typography.headlineSmall
            )
            HeightSpacer(6.dp)

            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Design System",
                    color = Material3Theme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = Material3Theme.typography.bodyMedium
                )
                Spacer(Modifier.weight(1f))

                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val designSystem = state.userProfileSetting?.personalization?.theme?.designSystem
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .width(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (designSystem) {
                                UserProfileSetting.Personalization.Theme.DESIGN_SYSTEM_MD3 -> "Google Material 3"
                                else -> "Unknown"
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
                    DropdownMenu(expanded, { expanded = false }, Modifier.width(180.dp)) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(designSystem != UserProfileSetting.Personalization.Theme.DESIGN_SYSTEM_MD3) { error("Not yet implemented") }
                                    .then(if (designSystem == UserProfileSetting.Personalization.Theme.DESIGN_SYSTEM_MD3) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Google Material 3",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedMode == "material3") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }
                        }
                    }
                }
            }
            HeightSpacer(8.dp)
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Color mode",
                    color = Material3Theme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = Material3Theme.typography.bodyMedium
                )
                Spacer(Modifier.weight(1f))

                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedMode = state.userProfileSetting?.personalization?.theme?.colorMode
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .width(160.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (selectedMode) {
                                "light" -> "Light"
                                "dark" -> "Dark"
                                "system" -> "System"
                                else -> "Unknown"
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
                    DropdownMenu(expanded, { expanded = false }, Modifier.width(160.dp)) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedMode != "light") { state.userSelectColorMode("light") }
                                    .then(if (selectedMode == "light") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Light",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedMode == "light") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedMode != "dark") { state.userSelectColorMode("dark") }
                                    .then(if (selectedMode == "dark") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Dark",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedMode == "dark") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }

                            val systemModeAvailable = true
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(systemModeAvailable && selectedMode != "system") { state.userSelectColorMode("system") }
                                    .then(if (selectedMode == "system") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "System",
                                    color = Material3Theme.colorScheme.onSurface.copy(
                                        alpha = if (systemModeAvailable) 1.0f else 0.38f
                                    ),
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            HeightSpacer(8.dp)
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Color seed",
                    color = Material3Theme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = Material3Theme.typography.bodyMedium
                )
                Spacer(Modifier.weight(1f))

                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedColorSeed = state.userProfileSetting?.personalization?.theme?.colorSeed
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .width(160.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier.clip(CircleShape).align(Alignment.CenterVertically).size(24.dp).background(
                                when (selectedColorSeed) {
                                    UserProfileSetting.Personalization.Theme.COLOR_SEED_GREEN -> Color(0xFF63A002)
                                    UserProfileSetting.Personalization.Theme.COLOR_SEED_ORANGE -> Color(0xFFb33b15)
                                    UserProfileSetting.Personalization.Theme.COLOR_SEED_BLUE -> Color(0xFF769CDF)
                                    UserProfileSetting.Personalization.Theme.COLOR_SEED_YELLOW -> Color(0xFFFFC107)
                                    UserProfileSetting.Personalization.Theme.COLOR_SEED_PURPLE -> Color(0xFF66009A)
                                    else -> Color.Transparent
                                }
                            )
                        )
                        WidthSpacer(12.dp)
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (selectedColorSeed) {
                                UserProfileSetting.Personalization.Theme.COLOR_SEED_GREEN -> "Green"
                                UserProfileSetting.Personalization.Theme.COLOR_SEED_ORANGE -> "Orange"
                                UserProfileSetting.Personalization.Theme.COLOR_SEED_BLUE -> "Blue"
                                UserProfileSetting.Personalization.Theme.COLOR_SEED_YELLOW -> "Yellow"
                                UserProfileSetting.Personalization.Theme.COLOR_SEED_PURPLE -> "Purple"
                                else -> "Unknown"
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
                    DropdownMenu(expanded, { expanded = false }, Modifier.width(160.dp)) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedColorSeed != UserProfileSetting.Personalization.Theme.COLOR_SEED_GREEN) { state.userSelectColorSeed(UserProfileSetting.Personalization.Theme.COLOR_SEED_GREEN) }
                                    .then(if (selectedColorSeed == UserProfileSetting.Personalization.Theme.COLOR_SEED_GREEN) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.clip(CircleShape).align(Alignment.CenterVertically).size(24.dp).background(
                                        Color(0xFF63A002)
                                    )
                                )
                                WidthSpacer(12.dp)
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Green",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedColorAccent == "green") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedColorSeed != UserProfileSetting.Personalization.Theme.COLOR_SEED_ORANGE) { state.userSelectColorSeed(UserProfileSetting.Personalization.Theme.COLOR_SEED_ORANGE) }
                                    .then(if (selectedColorSeed == UserProfileSetting.Personalization.Theme.COLOR_SEED_ORANGE) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.clip(CircleShape).align(Alignment.CenterVertically).size(24.dp).background(
                                        Color(0xFFb33b15)
                                    )
                                )
                                WidthSpacer(12.dp)
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Orange",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedColorAccent == "orange") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedColorSeed != UserProfileSetting.Personalization.Theme.COLOR_SEED_BLUE) { state.userSelectColorSeed(UserProfileSetting.Personalization.Theme.COLOR_SEED_BLUE) }
                                    .then(if (selectedColorSeed == UserProfileSetting.Personalization.Theme.COLOR_SEED_BLUE) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.clip(CircleShape).align(Alignment.CenterVertically).size(24.dp).background(
                                        Color(0xFF769CDF)
                                    )
                                )
                                WidthSpacer(12.dp)
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Blue",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedColorAccent == "blue") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedColorSeed != UserProfileSetting.Personalization.Theme.COLOR_SEED_YELLOW) { state.userSelectColorSeed(UserProfileSetting.Personalization.Theme.COLOR_SEED_YELLOW) }
                                    .then(if (selectedColorSeed == UserProfileSetting.Personalization.Theme.COLOR_SEED_YELLOW) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.clip(CircleShape).align(Alignment.CenterVertically).size(24.dp).background(
                                        Color(0xFFFFC107)
                                    )
                                )
                                WidthSpacer(12.dp)
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Yellow",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedColorAccent == "yellow") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedColorSeed != UserProfileSetting.Personalization.Theme.COLOR_SEED_PURPLE) { state.userSelectColorSeed(UserProfileSetting.Personalization.Theme.COLOR_SEED_PURPLE) }
                                    .then(if (selectedColorSeed == UserProfileSetting.Personalization.Theme.COLOR_SEED_PURPLE) Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.clip(CircleShape).align(Alignment.CenterVertically).size(24.dp).background(
                                        Color(0xFF66009A)
                                    )
                                )
                                WidthSpacer(12.dp)
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Purple",
                                    color = Material3Theme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                /*if (selectedColorAccent == "yellow") {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_check_medium_16px.png"),
                                        tint = Material3Theme.colorScheme.onSurfaceVariant,
                                        contentDescription = null,
                                    )
                                }*/
                            }
                        }
                    }
                }
            }
            HeightSpacer(8.dp)
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Font Family",
                    color = Material3Theme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = Material3Theme.typography.bodyMedium
                )
                Spacer(Modifier.weight(1f))
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    var selectedFontFamily by remember { mutableStateOf("roboto") }
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .width(160.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (selectedFontFamily) {
                                "roboto" -> "Roboto"
                                else -> "Unknown"
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
                    DropdownMenu(expanded, { expanded = false }, Modifier.width(160.dp)) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                Modifier.height(48.dp)
                                    .fillMaxWidth()
                                    .clickable(selectedFontFamily != "roboto") { selectedFontFamily = "roboto" }
                                    .then(if (selectedFontFamily == "roboto") Modifier.background(Material3Theme.colorScheme.secondaryContainer) else Modifier)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "Roboto",
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