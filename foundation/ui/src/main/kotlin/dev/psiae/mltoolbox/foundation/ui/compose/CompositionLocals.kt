package dev.psiae.mltoolbox.foundation.ui.compose

fun compositionLocalNotProvidedError(name: String): Nothing = error("composition local $name was not provided")
fun staticCompositionLocalNotProvidedError(name: String): Nothing = error("static composition local $name was not provided")