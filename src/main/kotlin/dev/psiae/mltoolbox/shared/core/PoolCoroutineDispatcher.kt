package dev.psiae.mltoolbox.shared.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

class PoolCoroutineDispatcher(
    private val pool: CoroutineDispatcher
) : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        return pool.dispatch(context, block)
    }

    @InternalCoroutinesApi
    override fun dispatchYield(context: CoroutineContext, block: Runnable) {
        return pool.dispatchYield(context, block)
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return pool.isDispatchNeeded(context)
    }

    @ExperimentalCoroutinesApi
    override fun limitedParallelism(parallelism: Int): CoroutineDispatcher {
        return pool.limitedParallelism(parallelism)
    }
}