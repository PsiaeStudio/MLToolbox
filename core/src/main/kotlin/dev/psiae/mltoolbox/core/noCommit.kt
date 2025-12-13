package dev.psiae.mltoolbox.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Remove or comment every usage before committing
 */
@Suppress("FunctionName")
@Deprecated(
    message = "No Commit, remove this usage",
    ReplaceWith(""),
    DeprecationLevel.WARNING
)
@OptIn(ExperimentalContracts::class)
inline fun noCommit(
    block: () -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

/**
 * Remove or comment every usage before compiling
 */
@Suppress("FunctionName")
@Deprecated(
    message = "No Compile, remove this usage",
    ReplaceWith(""),
    DeprecationLevel.ERROR
)
@OptIn(ExperimentalContracts::class)
inline fun noCompile(
    block: () -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}