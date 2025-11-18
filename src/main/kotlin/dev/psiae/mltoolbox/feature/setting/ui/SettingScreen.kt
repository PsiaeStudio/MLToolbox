package dev.psiae.mltoolbox.feature.setting.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.ui.composeui.HeightSpacer
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainDrawerDestination
import dev.psiae.mltoolbox.shared.ui.composeui.NoOpPainter
import dev.psiae.mltoolbox.shared.ui.composeui.StableList
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainScreenLayoutScreenHost
import dev.psiae.mltoolbox.shared.ui.composeui.md3.requireCurrent
import dev.psiae.mltoolbox.shared.ui.composeui.md3.rippleAlphaOrDefault
import dev.psiae.mltoolbox.feature.setting.personalization.ui.composeui.PersonalizationSettingScreen
import dev.psiae.mltoolbox.shared.main.ui.composeui.MainScreenState
import dev.psiae.mltoolbox.shared.ui.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.composeui.theme.md3.surfaceColorAtElevation
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme

@Composable
fun SettingScreen(
    mainScreenState: MainScreenState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .background(
                    animateColorAsState(
                        Material3Theme.colorScheme.surfaceContainer,
                        animationSpec = tween(200)
                    ).value
                )
                .defaultSurfaceGestureModifiers()
        )
        SettingScreenContent(rememberSettingScreenState(mainScreenState))
    }
}


@Composable
private fun SettingScreenContent(
    state: SettingScreenState
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            val personalization = personalizationSettingScreenDrawerItem(state)
            val dest = remember {
                mutableStateOf<StableList<MainDrawerDestination>>(StableList(listOf(personalization)), neverEqualPolicy())
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .widthIn(min = 120.dp, max = 160.dp)
                    .padding(end = 24.dp)
            ) {
                SettingScreenLayoutDrawerNavigationPanel(
                    state,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(1f)
                        .fillMaxHeight(),
                    onDestinationClicked = { select ->
                        if (!dest.value.any { it.id == select.id }) {
                            dest.value = StableList(
                                ArrayList<MainDrawerDestination>()
                                    .apply { addAll(dest.value) ; add(select) }
                            )
                        } else {
                            dest.value = StableList(
                                ArrayList<MainDrawerDestination>()
                                    .apply {
                                        dest.value.forEach {
                                            if (it.id != select.id) add(it)
                                        }
                                        add(select)
                                    }
                            )
                        }
                    },
                    currentDestinationId = dest.value.lastOrNull()?.id
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(1f),
                ) {
                    Box(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(1.dp, Material3Theme.colorScheme.outline, RoundedCornerShape(50))
                            .clip(RoundedCornerShape(50))
                            .clickable { state.userInputExitSetting() }
                            .padding(14.dp)
                    ) {
                        Icon(
                            modifier = Modifier.align(Alignment.Center).size(24.dp),
                            painter = painterResource("drawable/icon_home_outline_24px.png"),
                            contentDescription = null,
                            tint = Material3Theme.colorScheme.primary,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(60.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "v${MLToolboxApp.RELEASE_VERSION}",
                        style = Material3Theme.typography.bodySmall,
                        color = Material3Theme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            MainScreenLayoutScreenHost(dest.value)
        }
    }
}

@Composable
private fun personalizationSettingScreenDrawerItem(
    state: SettingScreenState
): MainDrawerDestination {
    val content = @Composable { PersonalizationSettingScreen(state) }
    val painter = painterResource("drawable/icon_personalization_brush_24px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "Personalization",
            icon = painter,
            iconTint = null,
            name = "Personalization",
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerNavigationPanelItem(
    modifier: Modifier,
    item: MainDrawerDestination,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val isDragged = interactionSource.collectIsDraggedAsState().value
    val selectedAnimationProgress = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(150)
    )
    Column(
        modifier = modifier
            .defaultMinSize(
                minHeight = if (item.icon != NoOpPainter) 56.dp else 46.dp,
                minWidth = 80.dp
            )
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(
                enabled = !isSelected && enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (item.icon != NoOpPainter) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 56.dp, minHeight = 32.dp)
                    .clip(RoundedCornerShape(50))
                    .composed {
                        val rippleTheme = LocalRippleConfiguration.requireCurrent()
                        if (isHovered) {
                            Modifier
                                .background(color = rippleTheme.color.copy(alpha = rippleTheme.rippleAlphaOrDefault().hoveredAlpha))
                        } else {
                            Modifier
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(
                            if (isSelected)
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Material3Theme.colorScheme.secondaryContainer)
                                    .graphicsLayer { alpha = selectedAnimationProgress.value }
                            else Modifier
                        )
                        .height(32.dp)
                        .width(56.dp * selectedAnimationProgress.value)
                )
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    painter = item.icon,
                    tint = item.iconTint ?: Material3Theme.colorScheme.onSecondaryContainer,
                    contentDescription = null
                )
            }
            HeightSpacer(4.dp)
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .alpha(if (enabled) 1f else 0.78f),
                text = item.name,
                style = Material3Theme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = Material3Theme.colorScheme.onSurface
            )
        } else {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 80.dp, minHeight = 46.dp)
                    .clip(RoundedCornerShape(50))
                    .composed {
                        val rippleTheme = LocalRippleConfiguration.requireCurrent()
                        if (isHovered) {
                            Modifier
                                .background(color = rippleTheme.color.copy(alpha = rippleTheme.rippleAlphaOrDefault().hoveredAlpha))
                        } else {
                            Modifier
                        }
                    }
            ) {
                var indicationWidth by remember { mutableStateOf(0.dp) }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(
                            if (isSelected)
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Material3Theme.colorScheme.secondaryContainer)
                                    .graphicsLayer { alpha = selectedAnimationProgress.value }
                            else Modifier
                        )
                        .height(46.dp)
                        .width(indicationWidth * selectedAnimationProgress.value)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .composed {
                            val density = LocalDensity.current
                            Modifier.onGloballyPositioned { coord ->
                                with(density) {
                                    indicationWidth = coord.size.width.toDp()
                                }
                            }
                        }
                        .defaultMinSize(minWidth = 100.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(if (enabled) 1f else 0.78f),
                        text = item.name,
                        style = Material3Theme.typography.labelLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        color = Material3Theme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingScreenLayoutDrawerNavigationPanel(
    state: SettingScreenState,
    modifier: Modifier,
    onDestinationClicked: (MainDrawerDestination) -> Unit,
    currentDestinationId: String?
) {
    Box(
        modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeightSpacer(16.dp)
            listOf(
                listOf(personalizationSettingScreenDrawerItem(state))
            ).run {
                fastForEachIndexed { i, group ->
                    group.fastForEachIndexed { ii, item ->
                        val isSelected = currentDestinationId == item.id
                        DrawerNavigationPanelItem(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .defaultMinSize(minWidth = 100.dp),
                            item = item,
                            isSelected = isSelected,
                            enabled = true,
                            onClick = { onDestinationClicked(item) }
                        )
                        if (ii < group.lastIndex) {
                            HeightSpacer(12.dp)
                        }
                    }
                    if (i < lastIndex) {
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.align(Alignment.Center).width(160.dp).padding(horizontal = 8.dp))
                        }
                    }
                }
            }
            HeightSpacer(16.dp)
        }
    }
}