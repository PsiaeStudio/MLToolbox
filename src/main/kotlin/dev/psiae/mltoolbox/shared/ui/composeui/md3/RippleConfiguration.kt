package dev.psiae.mltoolbox.shared.ui.composeui.md3

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal

@ExperimentalMaterial3Api
@Composable
fun CompositionLocal<RippleConfiguration?>.requireCurrent() = LocalRippleConfiguration.current
    ?: error("LocalRippleConfiguration was null")

@ExperimentalMaterial3Api
@Composable
fun RippleConfiguration.rippleAlphaOrDefault() = rippleAlpha ?: RippleDefaults.RippleAlpha