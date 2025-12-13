package dev.psiae.mltoolbox.shared.ui.compose

import androidx.compose.ui.unit.Constraints

fun Constraints.noMinConstraints() = copy(minWidth = 0, minHeight = 0)