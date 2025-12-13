package dev.psiae.mltoolbox.app.desktop

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.main.ui.MainGUI
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.shared.main.ui.DefaultExceptionWindow
import dev.psiae.mltoolbox.app.desktop.startup.DesktopStartup
import dev.psiae.mltoolbox.shared.main.ui.tryLockExceptionWindow
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    runCatching {
        DesktopStartup(args)
        MainGUI(MLToolboxApp.requireInstance())
    }.onFailure { thr ->
        try {
            val useExceptionWindow = tryLockExceptionWindow()
            Logger.tryLog { "Exception reached main, in thread '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
            if (!useExceptionWindow)
                return@onFailure
            DefaultExceptionWindow("Exception reached main, in thread '${Thread.currentThread().name}'", thr)
        } finally {
            exitProcess(1)
        }
    }
}