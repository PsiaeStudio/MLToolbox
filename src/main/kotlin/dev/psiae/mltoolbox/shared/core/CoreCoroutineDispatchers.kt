package dev.psiae.mltoolbox.shared.core

import kotlinx.coroutines.CoroutineDispatcher

class CoreCoroutineDispatchers(
    val main: MainCoroutineDispatcher,
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val unconfined: CoroutineDispatcher
)