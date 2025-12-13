package dev.psiae.mltoolbox.core.coroutine

import dev.psiae.mltoolbox.core.LazyConstructor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.MainCoroutineDispatcher

class AppCoroutineDispatchers(
    val main: MainCoroutineDispatcher,
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val unconfined: CoroutineDispatcher
) {
    companion object {
        private val INSTANCE = LazyConstructor<AppCoroutineDispatchers>()

        val io
            get() = INSTANCE.value.io

        fun construct(dispatchers: AppCoroutineDispatchers) = INSTANCE.constructOrThrow(
            {dispatchers},
            { error("AppCoroutineDispatchers already provided") }
        )
    }
}