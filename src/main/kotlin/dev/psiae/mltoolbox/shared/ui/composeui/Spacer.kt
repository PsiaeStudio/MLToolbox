package dev.psiae.mltoolbox.shared.ui.composeui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

// what's the difference between NonRestartable and inline ?

@Composable
@NonRestartableComposable
fun WidthSpacer(modifier: Modifier = Modifier, width: Dp) = Spacer(modifier.width(width))

@Composable
@NonRestartableComposable
fun WidthSpacer(width: Dp) = WidthSpacer(Modifier, width)

@Composable
@NonRestartableComposable
fun HeightSpacer(modifier: Modifier, height: Dp) = Spacer(modifier.height(height))

@Composable
@NonRestartableComposable
fun HeightSpacer(height: Dp) = HeightSpacer(Modifier, height)