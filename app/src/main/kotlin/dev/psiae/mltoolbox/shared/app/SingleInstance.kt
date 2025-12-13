package dev.psiae.mltoolbox.shared.app

import com.sun.nio.file.ExtendedOpenOption
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.shared.main.ui.DefaultExceptionWindow
import dev.psiae.mltoolbox.shared.main.ui.DefaultSimpleErrorWindow
import dev.psiae.mltoolbox.shared.main.ui.tryLockExceptionWindow
import dev.psiae.mltoolbox.shared.ui.mainUiDispatcher
import dev.psiae.mltoolbox.shared.ui.UiFoundation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes


object SingleInstance {
    const val APP_ID = MLToolboxApp.APP_ID
    const val FILE_LOCK_PATH = "${MLToolboxApp.DATA_DIR_PATH}\\process.lock"


    // start from non-ui dispatcher else deadlock
    @OptIn(DelicateCoroutinesApi::class)
    fun lock() = runCatching {
        runBlocking {
            val processLockJob = CompletableDeferred<Boolean>()

            GlobalScope.launch(UiFoundation.mainUiDispatcher.immediate) {
                async(Dispatchers.IO) {
                    runCatching {
                        val fileLock = jFile(FILE_LOCK_PATH)
                        if (!fileLock.parentFile.exists())
                            if (!jFile(FILE_LOCK_PATH).parentFile.mkdirs())
                                throw RuntimeException("Failed to mkdirs for process.lock")

                        val processLockFile = jFile(FILE_LOCK_PATH)
                        val fileChannel = runCatching {
                            FileChannel
                                .open(
                                    processLockFile.toPath(),
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.WRITE,
                                    ExtendedOpenOption.NOSHARE_READ,
                                    ExtendedOpenOption.NOSHARE_WRITE,
                                    ExtendedOpenOption.NOSHARE_DELETE
                                )
                        }.fold(
                            onSuccess = { it },
                            onFailure = { e ->
                                when (e) {
                                    is java.nio.file.FileSystemException, is kotlin.io.FileSystemException -> {
                                        Logger.tryLog {
                                            "unable to lock file, application already running, exiting"
                                        }

                                        // todo: bring focus
                                        processLockJob.complete(false)
                                        DefaultSimpleErrorWindow("Application is already running")
                                        exitProcess(1)
                                    }
                                    else -> {
                                        Logger.tryLog {
                                            "unable to acquire lock file: ${e.stackTraceToString()}"
                                        }
                                    }
                                }
                                throw e
                            }
                        )
                        processLockJob.complete(true)
                        // somebody please explain to me why tf it won't stay locked without these ?
                        // compiler ???
                        while (true) {
                            delay(Int.MAX_VALUE.toLong())
                            fileChannel.read(ByteBuffer.wrap(byteArrayOf()))
                        }
                    }.onFailure { thr ->
                        runCatching {
                            runCatching { processLockJob.complete(false) }
                            Logger.tryLog { "Exception during processLock '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
                            val useExceptionWindow = tryLockExceptionWindow()
                            if (!useExceptionWindow)
                                return@onFailure
                            DefaultExceptionWindow("Exception during processLock '${Thread.currentThread().name}': ", thr)
                        }
                        exitProcess(1)
                    }
                }.await()
            }
            runCatching {
                withTimeout(3500) { processLockJob.await() }
            }.fold(
                onSuccess = { locked ->
                    if (!locked) {
                        runCatching {
                            delay(1.minutes.inWholeMilliseconds)
                            Logger.tryLog { "Timeout waiting for process.lock failure to exit '${Thread.currentThread().name}'" }
                            val useExceptionWindow = tryLockExceptionWindow()
                            if (!useExceptionWindow)
                                return@fold
                            DefaultSimpleErrorWindow("Timeout waiting for process.lock failure to exit, this is a bug if there is no other app process already running '${Thread.currentThread().name}'")
                        }
                        exitProcess(1)
                    }
                },
                onFailure = { thr ->
                    if (thr is TimeoutCancellationException) {
                        runCatching {
                            delay(1.minutes.inWholeMilliseconds)
                            Logger.tryLog { "Timeout waiting for process.lock: '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
                            val useExceptionWindow = tryLockExceptionWindow()
                            if (!useExceptionWindow)
                                return@fold
                            DefaultExceptionWindow("Timeout waiting for process.lock '${Thread.currentThread().name}': ", thr)
                        }
                        exitProcess(1)
                    }
                    throw thr
                }
            )
        }
    }.onFailure { thr ->
        runCatching {
            Logger.tryLog { "SingleInstance: fail to processLock: '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
            val useExceptionWindow = tryLockExceptionWindow()
            if (!useExceptionWindow)
                return@onFailure
            DefaultExceptionWindow("SingleInstance: fail to processLock '${Thread.currentThread().name}': ", thr)
        }
        exitProcess(1)
    }
}