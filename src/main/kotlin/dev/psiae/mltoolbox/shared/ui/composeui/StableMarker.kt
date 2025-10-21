package dev.psiae.mltoolbox.shared.ui.composeui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable


typealias ComposeUiStableAny<T> = StableAny<T>
typealias ComposeUiImmutableAny<T> = ImmutableAny<T>

// remove this ?
@Stable
open class StableAny<T>(val value: T)

@Immutable
class ImmutableAny<T>(val value: T)

fun <T> T.wrapComposeUiStable() = ComposeUiStableAny(this)

fun <T> T.wrapComposeUiImmutable() = ImmutableAny(this)