package dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.foundation.ui.compose.HeightSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.WidthSpacer
import dev.psiae.mltoolbox.foundation.ui.compose.graphics.absoluteContentColor
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.foundation.ui.compose.theme.md3.Material3Theme
import dev.psiae.mltoolbox.shared.ui.compose.SimpleTooltip

@Composable
fun PlaysetListUi(
    state: PlaysetListUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
    ) {
        HeightSpacer(8.dp)
        Row(modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 4.dp, vertical = 4.dp)) { SearchBar(state) }
        HeightSpacer(4.dp)
        LazyList(state)
    }
}


@Composable
private fun SearchBar(
    state: PlaysetListUiState,
) {
    val enabled by remember {
        mutableStateOf(true)
    }
    var active by remember {
        mutableStateOf(false)
    }
    val query by remember {
        derivedStateOf { state.lastQuery }
    }
    var isError by remember {
        mutableStateOf(false)
    }
    val ins = remember { MutableInteractionSource() }

    val surfaceColor = if (LocalIsDarkTheme.current)
        Material3Theme.colorScheme.surfaceContainerHigh
    else
        Material3Theme.colorScheme.surfaceBright
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = surfaceColor,
        unfocusedContainerColor = surfaceColor,
        errorContainerColor = surfaceColor,
        errorIndicatorColor = Material3Theme.colorScheme.error,
    )
    Column {
        BasicTextField(
            modifier = Modifier
                .defaultMinSize(350.dp, 24.dp)
                .sizeIn(maxHeight = 35.dp),
            singleLine = true,
            maxLines = 1,
            value = query,
            onValueChange = { state.queryChange(it) },
            interactionSource = ins,
            textStyle = Material3Theme.typography.labelMedium.copy(
                color = Material3Theme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            ),
            keyboardOptions = KeyboardOptions.Default,
            cursorBrush = SolidColor(Material3Theme.colorScheme.onSurface),
            visualTransformation = VisualTransformation.None,
            decorationBox = {
                @OptIn(ExperimentalMaterial3Api::class)
                (TextFieldDefaults.DecorationBox(
                    value = query,
                    innerTextField = {
                        Box(
                            modifier = Modifier
                                .sizeIn(350.dp),
                            content = { it() }
                        )
                    },
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = ins,
                    colors = colors,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 4.dp),
                    container = {
                        val color by run {
                            val focused by ins.collectIsFocusedAsState()

                            val targetValue = with(colors) { when {
                                !enabled -> disabledContainerColor
                                isError -> errorContainerColor
                                focused -> focusedContainerColor
                                else -> unfocusedContainerColor
                            } }
                            key(surfaceColor) {
                                animateColorAsState(targetValue, tween(durationMillis = 150))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .then(
                                    if (LocalIsDarkTheme.current)
                                        Modifier.shadow(elevation = 2.dp, RoundedCornerShape(50))
                                    else
                                        Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(50))
                                )
                                .background(color)
                        )
                    },
                    label = null,
                    leadingIcon = {
                        Icon(
                            painter = painterResource("drawable/icon_search_16px.png"),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Material3Theme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = null,
                    shape = RoundedCornerShape(50),
                    placeholder = {
                        Box(
                            modifier = Modifier.defaultMinSize(350.dp)
                        ) {
                            if (query.isEmpty() and !ins.collectIsFocusedAsState().value) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    text = "Search mod",
                                    color = Material3Theme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge.copy(
                                        baselineShift = BaselineShift(-0.1f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                ))
            },
        )
    }
}

@Composable
private fun LazyList(
    state: PlaysetListUiState
) {
    val scrollState = rememberLazyListState()
    Row {
        LazyColumn(
            modifier = Modifier
                .weight(1f, false)
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            val list = if (state.queryParamsEnabled)
                state.queriedInstalledPlaysetList
            else
                state.filteredInstalledPlaysetList
            itemsIndexed(
                key = { i, modData -> modData.uniqueQualifiedName },
                items = list
            ) { i, modData ->
                Row(
                    Modifier.defaultMinSize(minHeight = 48.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = modData.name,
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyLarge
                                .copy(baselineShift = BaselineShift(-0.1f)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        WidthSpacer(8.dp)
                        var tagsRenderIndex = 0

                        if (modData.isUE4SS) {
                            if (tagsRenderIndex++ > 0)
                                WidthSpacer(4.dp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Material3Theme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "UE4SS",
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.labelSmall
                                        .copy(baselineShift = BaselineShift(-0.1f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        if (modData.isUEPak) {
                            if (tagsRenderIndex++ > 0)
                                WidthSpacer(4.dp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Material3Theme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "UE Pak",
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.labelSmall
                                        .copy(baselineShift = BaselineShift(-0.1f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (modData.isUEPak) {
                            SimpleTooltip(
                                "delete"
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clickable { state.deleteMod(modData) }
                                        .padding(8.dp)
                                        .size(24.dp),
                                    painter = painterResource("drawable/icon_delete_trash_outline_24px.png"),
                                    tint = Material3Theme.colorScheme.onSurface,
                                    contentDescription = null
                                )
                            }
                        }
                        Checkbox(
                            checked = modData.isEnabled,
                            onCheckedChange = { checked -> state.toggleMod(modData) },
                            colors = CheckboxDefaults.colors(),
                            enabled = modData.isUE4SS
                        )
                        /*Icon(
                            modifier = Modifier
                                .alpha(0.38f)
                                .clickable(enabled = false) { }
                                .padding(8.dp)
                                .size(24.dp),
                            painter = painterResource("drawable/icon_more_24px.png"),
                            tint = Material3Theme.colorScheme.onSurface,
                            contentDescription = null
                        )*/
                    }
                }
                if (i < list.lastIndex)
                    HeightSpacer(12.dp)
            }
            /*item(
                key = "bottomContentPadding"
            ) {
                HeightSpacer(12.dp)
            }*/
        }
        WidthSpacer(4.dp)
        VerticalScrollbar(
            modifier = Modifier
                .height(
                    with(LocalDensity.current) {
                        remember(this) {
                            derivedStateOf { scrollState.layoutInfo.viewportSize.height.toDp() }
                        }.value
                    }
                )
                .padding(start = 0.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
                .then(
                    if (scrollState.canScrollForward or scrollState.canScrollBackward)
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