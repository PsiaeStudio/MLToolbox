package dev.psiae.mltoolbox.core

import java.util.concurrent.Executors

private object Executor {

    val INSTANCE = Executors.newSingleThreadExecutor {  r -> Core.constructMainThread { Thread(r) } }
}

val Core.MainExecutor get() = Executor.INSTANCE