package dev.psiae.mltoolbox.shared.logger

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import saschpe.log4k.FileLogger
import saschpe.log4k.Log

typealias Log4k = saschpe.log4k.Log

object Logger {

    init {
        val fileLogger = FileLogger(rotate = FileLogger.Rotate.Daily, limit = FileLogger.Limit.Files(28), logPath = MLToolboxApp.logsDir.absolutePath)
        Log4k.loggers.add(fileLogger)
    }

    fun log(msg: String) {
        Log.log(Log.Level.Info, "Tag", null, msg)
    }

    // Logger should already be initialized before GUI
    fun tryLog(getMsg: () -> String): Boolean = runCatching {
        Log.log(Log.Level.Info, "Tag", null, getMsg())
    }.isSuccess
}