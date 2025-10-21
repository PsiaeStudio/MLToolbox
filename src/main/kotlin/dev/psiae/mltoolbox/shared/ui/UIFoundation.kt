package dev.psiae.mltoolbox.shared.ui

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.core.MainImmediateCoroutineDispatcher
import dev.psiae.mltoolbox.shared.utils.LazyConstructor
import dev.psiae.mltoolbox.shared.utils.valueOrNull
import kotlinx.coroutines.*
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext

object UIFoundation {
}

val UIFoundation.MainUIDispatcher: MainCoroutineDispatcher get() = Swing.MAIN_DISPATCHER
val UIFoundation.MainImmediateUIDispatcher: MainImmediateCoroutineDispatcher get() = Swing.MAIN_DISPATCHER_IMMEDIATE
val UIFoundation.mainThread
    get() = Swing.LAZY_MAIN_THREAD.valueOrNull()
        ?: error("UIFoundation.Swing.LAZY_MAIN_THREAD not provided")

fun UIFoundation.provideMainThread() = Swing.LAZY_MAIN_THREAD.constructOrThrow(
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

    val LAZY_MAIN_THREAD = LazyConstructor<java.lang.Thread>()

    @OptIn(InternalCoroutinesApi::class)
    private class SwingMainCoroutineDispatcher(

    ):
        MainCoroutineDispatcher(),
        Delay by org.jetbrains.skiko.MainUIDispatcher as Delay {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            org.jetbrains.skiko.MainUIDispatcher.dispatch(context, block)
        }

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
            else nonImmediate.dispatch(context, block)
        }

        override val nonImmediate: CoroutineDispatcher
            get() = MAIN_DISPATCHER

        override val immediate: MainImmediateCoroutineDispatcher
            get() = this
    }
}