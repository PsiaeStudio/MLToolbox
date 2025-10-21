package dev.psiae.mltoolbox.shared.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlin.coroutines.CoroutineContext

typealias KotlinxMainCoroutineDispatcher = kotlinx.coroutines.MainCoroutineDispatcher

private object Dispatcher {

    val EXECUTOR = Core.MainExecutor.asCoroutineDispatcher()

    val DISPATCHER: MainCoroutineDispatcher = MainCoroutineDispatcherImpl()
    val DISPATCHER_IMMEDIATE: MainImmediateCoroutineDispatcher = MainImmediateCoroutineDispatcherImpl()

    private class MainCoroutineDispatcherImpl(): MainCoroutineDispatcher() {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            EXECUTOR.dispatch(context, block)
        }

        override val immediate: MainImmediateCoroutineDispatcher
            get() = DISPATCHER_IMMEDIATE
    }

    class MainImmediateCoroutineDispatcherImpl() : MainImmediateCoroutineDispatcher() {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (Thread.currentThread() == Core.MainThread)
                block.run()
            else
                EXECUTOR.dispatch(context, block)
        }

        override val nonImmediate: CoroutineDispatcher
            get() = DISPATCHER

        override val immediate: MainImmediateCoroutineDispatcher
            get() = this
    }
}

abstract class MainCoroutineDispatcher : KotlinxMainCoroutineDispatcher() {

    abstract override val immediate: MainImmediateCoroutineDispatcher
}

abstract class MainImmediateCoroutineDispatcher : MainCoroutineDispatcher() {


    abstract val nonImmediate: CoroutineDispatcher
}

val Core.MainDispatcher
    get() = Dispatcher.DISPATCHER

val Core.MainImmediateDispatcher
    get() = Dispatcher.DISPATCHER_IMMEDIATE

fun MainCoroutineDispatcher.immediateOrDispatching() = immediate ?: this