package dev.psiae.mltoolbox.foundation.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import coil3.compose.rememberAsyncImagePainter

@Composable
@NonRestartableComposable
fun WidthSpacer(modifier: Modifier = Modifier.Companion, width: Dp) = Spacer(modifier.width(width))

@Composable
@NonRestartableComposable
fun WidthSpacer(width: Dp) = WidthSpacer(Modifier.Companion, width)

@Composable
@NonRestartableComposable
fun HeightSpacer(modifier: Modifier, height: Dp) = Spacer(modifier.height(height))

@Composable
@NonRestartableComposable
fun HeightSpacer(height: Dp) = HeightSpacer(Modifier.Companion, height)