package dev.psiae.mltoolbox.foundation.ui.nav

import dev.psiae.mltoolbox.foundation.ui.Screen

object NavEntryIdGenerator {
    private var nextId = 0L

    fun generate(screen: Screen): NavId {
        return NavId(
            buildString {
                append("navid_")
                append(screen.name)
                append("_")
                append(nextId++)
            }
        )
    }
}