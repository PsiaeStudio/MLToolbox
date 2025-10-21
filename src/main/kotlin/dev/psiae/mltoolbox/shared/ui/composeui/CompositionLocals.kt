package dev.psiae.mltoolbox.shared.ui.composeui

fun compositionLocalNotProvidedError(name: String): Nothing = error("composition local $name was not provided")