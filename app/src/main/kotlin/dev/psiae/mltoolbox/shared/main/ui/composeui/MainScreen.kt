package dev.psiae.mltoolbox.shared.main.ui.composeui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEachIndexed
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.ui.compose.*
import dev.psiae.mltoolbox.feature.gamemanager.ui.composeui.gameManagerMainScreenDrawerItem
import dev.psiae.mltoolbox.shared.ui.compose.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.shared.ui.compose.md3.requireCurrent
import dev.psiae.mltoolbox.shared.ui.compose.md3.rippleAlphaOrDefault
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.modManagerMainScreenDrawerItem
import dev.psiae.mltoolbox.feature.moddingtool.ui.composeui.modForgeScreenDrawerItem
import dev.psiae.mltoolbox.feature.setting.ui.SettingScreen
import dev.psiae.mltoolbox.feature.supportproject.ui.composeui.supportProjectMainScreenDrawerItem
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.graphics.painter.NoopPainter
import dev.psiae.mltoolbox.foundation.ui.compose.thenIf
import dev.psiae.mltoolbox.foundation.ui.compose.visibilityGone
import dev.psiae.mltoolbox.shared.ui.compose.text.nonScaledFontSize
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.compose.theme.md3.ripple
import dev.psiae.mltoolbox.shared.platform.win32.CustomDecorationParameters
import dev.psiae.mltoolbox.shared.ui.compose.theme.md3.colorSchemeForSeedCode
import dev.psiae.mltoolbox.shared.ui.md3.MD3Spec
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import dev.psiae.mltoolbox.shared.ui.md3.margin
import dev.psiae.mltoolbox.shared.ui.md3.spacingOfWindowWidthDp
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting
import kotlin.math.max

@Composable
fun MainScreen() {
    val state = rememberMainScreenState()
    MainScreen(state)
}

@Composable
fun MainScreen(
    state: MainScreenState
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF161616))
    ) {
        if (state.isReady) {
            CompositionLocalProvider(
                LocalIsDarkTheme provides state.isThemeDark
            ) {
                MaterialTheme(
                    colorScheme = MD3Theme.colorSchemeForSeedCode(
                        state.userProfileSetting?.personalization?.theme?.colorSeed.toString(),
                        LocalIsDarkTheme.current
                    )
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides Material3Theme.colorScheme.onSurface,
                        LocalRippleConfiguration provides RippleConfiguration(Material3Theme.colorScheme.onSurface)
                    ) {
                        MainScreenLayoutSurface(
                            modifier = Modifier,
                            color = animateColorAsState(
                                Material3Theme.colorScheme.surfaceContainer,
                                animationSpec = tween(200)
                            ).value,
                        )
                        MainScreenLayoutContent(
                            state,
                            contentPadding = run {
                                val margin = MD3Spec.margin.spacingOfWindowWidthDp(maxWidth.value).dp
                                PaddingValues(margin, 0.dp, margin, 0.dp)
                            }
                        )
                    }
                }
            }
        }
        CompositionLocalProvider(
            LocalIsDarkTheme provides true
        ) {
            MaterialTheme(
                colorScheme = MD3Theme.colorSchemeForSeedCode(
                    UserProfileSetting.Personalization.Theme.COLOR_SEED_GREEN,
                    LocalIsDarkTheme.current
                )
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides Material3Theme.colorScheme.onSurface,
                    LocalRippleConfiguration provides RippleConfiguration(Material3Theme.colorScheme.onSurface)
                ) {
                    val targetValue = if (state.isReady) 0f else 1f
                    val animation = if (targetValue == 1f) snap<Float>(0) else tween(300)
                    val alpha by animateFloatAsState(targetValue, animation)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(alpha)
                            .background(Color(0xFF161616))
                            .align(Alignment.Center)
                            .then(if (alpha != 0f) Modifier.defaultSurfaceGestureModifiers() else Modifier)
                    ) {
                        ContainedLoadingIndicator(
                            Modifier.align(Alignment.Center).size(56.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenLayoutSurface(
    modifier: Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .defaultSurfaceGestureModifiers()
    )
}

@Composable
fun MainScreenLayoutContent(
    state: MainScreenState,
    contentPadding: PaddingValues
) {
    val leftPadding = contentPadding.calculateLeftPadding(LayoutDirection.Ltr)
    val rightPadding = contentPadding.calculateRightPadding(LayoutDirection.Ltr)
    Column(
        modifier = Modifier
            .layout { measurable, constraints ->
                val rtlAware = false
                val start = leftPadding
                val end = rightPadding
                val top = 0.dp
                val bottom = 0.dp
                val horizontal = start.roundToPx() + end.roundToPx()
                val vertical = top.roundToPx() + bottom.roundToPx()

                val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

                val width = constraints.constrainWidth(placeable.width + horizontal)
                val height = constraints.constrainHeight(placeable.height + vertical)
                layout(width, height) {
                    if (rtlAware) {
                        placeable.placeRelative(start.roundToPx(), top.roundToPx())
                    } else {
                        placeable.place(start.roundToPx(), top.roundToPx())
                    }
                    CustomDecorationParameters.controlBoxRightOffset = end.roundToPx()
                }
            }
    ) {
        CompositionLocalProvider(LocalIndication provides MD3Theme.ripple()) {
            MainScreenLayoutTopBar(contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()))
            MainScreenLayoutBody(state)
        }
    }
}


@Composable
fun MainScreenLayoutTopBar(
    contentPadding: PaddingValues = PaddingValues()
) {
    val titleBarBehavior = LocalTitleBarBehavior.current as CustomWin32TitleBarBehavior
    Box(
        modifier = Modifier
    ) {
        Layout(
            modifier = Modifier.height(48.dp),
            content = {
                MainScreenLayoutIconTitle(
                    modifier = Modifier.layoutId("ic")
                )
                MainScreenLayoutCaptionControls(
                    modifier = Modifier.layoutId("cpc")
                )
            }
        ) { measurable, inConstraint ->
            val constraint = inConstraint.noMinConstraints()
            val icon = measurable.fastFirst { it.layoutId == "ic" }
            val iconMeasure = icon.measure(constraint)
            val cpc = measurable.fastFirst { it.layoutId == "cpc" }
            val cpcMeasure = cpc.measure(constraint)

            var maxH = max(iconMeasure.height, cpcMeasure.height)
            val alignment = BiasAlignment.Vertical(0f)

            layout(
                height = constraint.maxHeight,
                width = constraint.maxWidth
            ) {
                iconMeasure.place(
                    x = 0,
                    y = alignment.align(iconMeasure.height, constraint.maxHeight)
                )
                cpcMeasure.place(
                    x = constraint.maxWidth.minus(cpcMeasure.width),
                    y = alignment.align(cpcMeasure.height, constraint.maxHeight)
                )

                CustomDecorationParameters
                    .apply {
                        titleBarHeight = constraint.maxHeight
                        controlBoxTopOffset = alignment.align(cpcMeasure.height, constraint.maxHeight)
                    }

                titleBarBehavior
                    .apply {
                        titleBarHeightPx = constraint.maxHeight
                    }
            }
        }
    }
}

@Composable
private fun MainScreenLayoutIconTitle(
    modifier: Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        /*Icon(
            modifier = Modifier
                .sizeIn(maxWidth = 64.dp, maxHeight = 36.dp),
            painter = painterResource("drawable/icon_manorlords_logo_text.png"),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))*/
        Text(
            modifier = Modifier,
            text = "MANOR LORDS Toolbox",
            style = Material3Theme.typography.titleMedium,
            fontSize = Material3Theme.typography.titleMedium.nonScaledFontSize(),
            color = Material3Theme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MainScreenLayoutCaptionControls(
    modifier: Modifier
) {
    val titleBarBehavior = LocalTitleBarBehavior.current
    Row(
        modifier = modifier.layout { measurable, constraints ->

            val measure = measurable.measure(constraints)

            CustomDecorationParameters
                .apply {
                    controlBoxWidth = measure.width
                    controlBoxHeight = measure.height
                }

            layout(measure.width, measure.height) {
                measure.place(0, 0)
            }
        }
    ) {
        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.minimizeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_minimize_win1.png"),
                contentDescription = null,
                tint = Material3Theme.colorScheme.onSurface
            )
        }

        run {
            // keep lambda and painter in sync
            val showRestore = titleBarBehavior.showRestoreWindow
            Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
                if (showRestore) titleBarBehavior.restoreClicked() else titleBarBehavior.maximizeClicked()
            }) {
                Icon(
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    painter = if (!showRestore)
                        painterResource("drawable/windowcontrol_maximized_win.png")
                    else
                        painterResource("drawable/windowcontrol_restore_down.png"),
                    contentDescription = null,
                    tint = Material3Theme.colorScheme.onSurface
                )
            }
        }

        Box(modifier = Modifier.width(40.dp).height(30.dp)) {
            val interactionSource = remember { MutableInteractionSource() }
            val warn = interactionSource.collectIsHoveredAsState().value or interactionSource.collectIsFocusedAsState().value
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (warn) Modifier.background(Material3Theme.colorScheme.errorContainer) else Modifier)
                    .clickable(interactionSource = interactionSource) {
                        titleBarBehavior.closeClicked()
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    painter = painterResource("drawable/windowcontrol_close2.png"),
                    contentDescription = null,
                    tint = Material3Theme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreenLayoutBody(
    state: MainScreenState
) {
    BoxWithConstraints(
        modifier = Modifier
    ) {
        val maxWidth = maxWidth
        Row(
            modifier = Modifier
                .thenIf(state.openSetting) {
                    Modifier.visibilityGone()
                }
        ) {
            val modManagerDest = modManagerMainScreenDrawerItem()
            val dest = remember { mutableStateOf<StableList<MainDrawerDestination>>(StableList(listOf(modManagerDest)), neverEqualPolicy()) }
            Column(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .widthIn(min = 120.dp, max = 160.dp)
                    .padding(end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    MainScreenLayoutDrawerNavigationPanel(
                        modifier = Modifier
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
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(1.dp, Material3Theme.colorScheme.outline, RoundedCornerShape(50))
                            .clip(RoundedCornerShape(50))
                            .clickable { state.userInputOpenSetting() }
                            .padding(14.dp)
                    ) {
                        val isDarkTheme = LocalIsDarkTheme.current

                        Icon(
                            modifier = Modifier.align(Alignment.Center).size(24.dp),
                            painter = painterResource("drawable/icon_settings_outline_24px.png"),
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
                        text = "v${MLToolboxApp.version}",
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

        if (state.openSetting) {
            SettingScreen(state)
        }
    }
}

@Composable
fun MainScreenLayoutDrawerNavigationPanel(
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
                listOf(
                    gameManagerMainScreenDrawerItem(),
                    modManagerMainScreenDrawerItem(),
                    modForgeScreenDrawerItem()
                ),
                listOf(supportProjectMainScreenDrawerItem())
            ).run {
                fastForEachIndexed { i, group ->
                    group.fastForEachIndexed { ii, item ->
                        val isSelected = currentDestinationId == item.id
                        DrawerNavigationPanelItem(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 100.dp),
                            item = item,
                            isSelected = isSelected,
                            enabled = true,
                            onClick = { onDestinationClicked(item) }
                        )
                        if (ii < group.lastIndex) {
                            HeightSpacer(16.dp)
                        }
                    }
                    if (i < lastIndex) {
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.align(Alignment.Center).width(100.dp).padding(horizontal = 8.dp))
                        }
                    }
                }
            }
            HeightSpacer(16.dp)
        }
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
                minHeight = if (item.icon != NoopPainter) 56.dp else 46.dp,
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
        if (item.icon != NoopPainter) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 56.dp, minHeight = 32.dp)
                    .clip(RoundedCornerShape(50))
                    .composed {
                        if (isHovered || isFocused) {
                            Modifier
                                .background(color = Material3Theme.colorScheme.onSecondaryContainer.copy(alpha = if (isHovered) 0.08f else 0.1f))
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
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(if (enabled) 1f else 0.78f),
                        text = item.name,
                        style = Material3Theme.typography.labelMedium,
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
fun HostNoDestinationSelected() {

}