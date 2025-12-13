package dev.psiae.mltoolbox.shared.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun WorkInProgressScreen(
    modifier: Modifier
) = Box(modifier.fillMaxSize()) {

    Text(
        modifier = Modifier.align(Alignment.Center),
        text = "WORK IN PROGRESS",
        color = Color(250, 250, 250).copy(alpha = 0.78f),
        maxLines = 1,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun PlannedScreen(
    modifier: Modifier
) = Box(modifier.fillMaxSize()) {

    Text(
        modifier = Modifier.align(Alignment.Center),
        text = "PLANNED FEATURE",
        color = Color(250, 250, 250).copy(alpha = 0.78f),
        maxLines = 1,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.SemiBold
    )
}