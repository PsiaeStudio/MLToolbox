package dev.psiae.mltoolbox.shared.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// https://github.com/Kotlin/kotlinx.coroutines/issues/1686
// https://gist.github.com/elizarov/9a48b9709ffd508909d34fab6786acfe

// note: Reentrant mutex that simply works on coroutine is not possible

class ReentrantMutex(
    private val mutex: Mutex = Mutex()
): Mutex by mutex,
    CoroutineContext.Key<ReentrantMutex>,
    CoroutineContext.Element {

    override val key get() = this

    suspend inline fun <T> withReentrantLock(crossinline block: suspend () -> T): T {
        return currentCoroutineContext()[this]
            ?.let { block() }
            ?: withContext(this) { withLock { block() } }
    }
}