package dev.psiae.mltoolbox.shared.ui.composeui

import androidx.compose.ui.unit.Constraints

fun Constraints.noMinConstraints() = copy(minWidth = 0, minHeight = 0)