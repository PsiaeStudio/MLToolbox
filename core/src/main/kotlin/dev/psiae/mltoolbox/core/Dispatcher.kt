package dev.psiae.mltoolbox.core

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.MainCoroutineDispatcher as KotlinxMainCoroutineDispatcher

private object Dispatcher {

    val mainExecutorDispatcher = Core.MainExecutor.asCoroutineDispatcher()

    val dispatching: MainCoroutineDispatcher = MainCoroutineDispatcherImpl()
    val immediate: MainImmediateCoroutineDispatcher = MainImmediateCoroutineDispatcherImpl()

    private class MainCoroutineDispatcherImpl(): MainCoroutineDispatcher() {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            mainExecutorDispatcher.dispatch(context, block)
        }

        override val dispatching: CoroutineDispatcher
            get() = this

        override val immediate: MainImmediateCoroutineDispatcher
            get() = Dispatcher.immediate
    }

    private class MainImmediateCoroutineDispatcherImpl() : MainImmediateCoroutineDispatcher() {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (Thread.currentThread() == Core.MainThread)
                block.run()
            else
                mainExecutorDispatcher.dispatch(context, block)
        }

        override val dispatching: CoroutineDispatcher
            get() = Dispatcher.dispatching

        override val immediate: MainImmediateCoroutineDispatcher
            get() = this
    }
}

abstract class MainCoroutineDispatcher : KotlinxMainCoroutineDispatcher() {

    abstract val dispatching: CoroutineDispatcher
    abstract override val immediate: MainImmediateCoroutineDispatcher
}

abstract class MainImmediateCoroutineDispatcher : MainCoroutineDispatcher() {



}

val Core.mainDispatchingDispatcher
    get() = Dispatcher.dispatching

val Core.mainImmediateDispatcher
    get() = Dispatcher.immediate

val Core.dispatcher
    get() = Dispatcher.immediate