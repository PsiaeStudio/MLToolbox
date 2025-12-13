package dev.psiae.mltoolbox.shared.ui.compose

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// remove any usage of this function when the issue is fixed
@OptIn(ExperimentalContracts::class)
inline fun composeEarlyReturn(
    block: () -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    run(block)
}