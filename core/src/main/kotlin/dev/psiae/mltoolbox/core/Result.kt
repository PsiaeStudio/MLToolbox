package dev.psiae.mltoolbox.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Catch exception in [block] by [return/jumps](https://kotlinlang.org/docs/returns.html) otherwise rethrow
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Result<T>.catchOrRethrow(block: (Throwable) -> Unit): Result<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return onFailure { e ->
        block(e)
        throw e
    }
}