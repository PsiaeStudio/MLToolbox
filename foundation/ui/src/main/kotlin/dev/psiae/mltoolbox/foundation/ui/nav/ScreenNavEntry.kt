package dev.psiae.mltoolbox.foundation.ui.nav

import dev.psiae.mltoolbox.foundation.ui.Screen

data class ScreenNavEntry(
    val screen: Screen,
    val id: NavId
) {

}

fun ScreenNavEntry(screen: Screen) = ScreenNavEntry(
    screen,
    NavEntryIdGenerator.generate(screen)
)