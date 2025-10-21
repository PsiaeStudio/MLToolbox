package dev.psiae.mltoolbox.shared.core

import dev.psiae.mltoolbox.shared.utils.LazyConstructor

typealias jThread = java.lang.Thread

private object Thread {
    val INSTANCE = LazyConstructor<java.lang.Thread>()
}

val Core.MainThread get() = Thread.INSTANCE.value

fun Core.constructMainThread(
    block: () -> java.lang.Thread
) = Thread.INSTANCE.constructOrThrow(
    lazyValue = block,
    lazyThrow = { error("Core.MainThread was already initialized") }
)