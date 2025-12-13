package dev.psiae.mltoolbox.core.logger

import saschpe.log4k.FileLogger
import saschpe.log4k.Log

typealias Log4k = Log

object Logger {

    fun init(
        path: String
    ) {
        val fileLogger = FileLogger(rotate = FileLogger.Rotate.Daily, limit = FileLogger.Limit.Files(28), logPath = path)
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