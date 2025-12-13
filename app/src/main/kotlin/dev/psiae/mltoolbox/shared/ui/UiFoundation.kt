package dev.psiae.mltoolbox.shared.ui

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.core.MainCoroutineDispatcher
import dev.psiae.mltoolbox.core.MainImmediateCoroutineDispatcher
import dev.psiae.mltoolbox.core.LazyConstructor
import dev.psiae.mltoolbox.core.valueOrNull
import kotlinx.coroutines.*
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext

object UiFoundation {
}

val UiFoundation.mainUiDispatcher: MainCoroutineDispatcher get() = Swing.MAIN_DISPATCHER
val UiFoundation.mainImmediateUiDispatcher: MainImmediateCoroutineDispatcher get() = Swing.MAIN_DISPATCHER_IMMEDIATE
val UiFoundation.mainThread
    get() = Swing.LAZY_MAIN_THREAD.valueOrNull()
        ?: error("UIFoundation.Swing.LAZY_MAIN_THREAD not provided")

fun UiFoundation.provideMainThread() = Swing.LAZY_MAIN_THREAD.constructOrThrow(
    lazyThrow = {
        error("UIFoundation.LAZY_MAIN_THREAD already provided")
    },
    lazyValue = {
        check(SwingUtilities.isEventDispatchThread()) {
            "UIFoundation.provideMainThread must be called from UI thread"
        }
        Thread.currentThread()
    }
)

private object Swing {

    val MAIN_DISPATCHER: MainCoroutineDispatcher = SwingMainCoroutineDispatcher()
    val MAIN_DISPATCHER_IMMEDIATE: MainImmediateCoroutineDispatcher = SwingMainImmediateDispatcher()
    val MAIN_GLOBAL_SCOPE = CoroutineScope(MAIN_DISPATCHER + SupervisorJob(MLToolboxApp.coroutineLifetimeJob))

    val LAZY_MAIN_THREAD = LazyConstructor<Thread>()

    @OptIn(InternalCoroutinesApi::class)
    private class SwingMainCoroutineDispatcher(

    ):
        MainCoroutineDispatcher(),
        Delay by org.jetbrains.skiko.MainUIDispatcher as Delay {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            org.jetbrains.skiko.MainUIDispatcher.dispatch(context, block)
        }

        override val dispatching: CoroutineDispatcher
            get() = MAIN_DISPATCHER

        override val immediate: MainImmediateCoroutineDispatcher
            get() = MAIN_DISPATCHER_IMMEDIATE
    }

    @OptIn(InternalCoroutinesApi::class)
    private class SwingMainImmediateDispatcher(

    ) :
        MainImmediateCoroutineDispatcher(),
        Delay by MAIN_DISPATCHER as Delay {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (SwingUtilities.isEventDispatchThread()) block.run()
            else dispatching.dispatch(context, block)
        }

        override val dispatching: CoroutineDispatcher
            get() = MAIN_DISPATCHER

        override val immediate: MainImmediateCoroutineDispatcher
            get() = this
    }
}