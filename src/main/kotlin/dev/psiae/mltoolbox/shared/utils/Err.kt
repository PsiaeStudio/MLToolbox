package dev.psiae.mltoolbox.shared.utils

fun runtimeError(message: String, cause: Throwable? = null): Nothing = throw RuntimeException(message, cause)
fun unsupportedOperationError(message: String, cause: Throwable? = null): Nothing = throw UnsupportedOperationException(message, cause)