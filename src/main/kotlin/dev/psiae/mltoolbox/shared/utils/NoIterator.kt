package dev.psiae.mltoolbox.shared.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// optimize iteration on collection such as ArrayList
// do note that certain collection structure such as Node based LinkedList will be much slower

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Array<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

