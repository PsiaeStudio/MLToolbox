package dev.psiae.mltoolbox.app.desktop.startup

import dev.psiae.mltoolbox.Main
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.app.SingleInstance
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.shared.libs.NativeLibsInitializer
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.shared.main.ui.DefaultExceptionWindow
import dev.psiae.mltoolbox.shared.main.ui.tryLockExceptionWindow
import dev.psiae.mltoolbox.shared.startup.AppInitializer
import dev.psiae.mltoolbox.shared.ui.mainImmediateUiDispatcher
import dev.psiae.mltoolbox.shared.ui.UiFoundation
import dev.psiae.mltoolbox.shared.ui.provideMainThread
import dev.psiae.mltoolbox.shared.utils.castOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.lang.Thread
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.SwingUtilities
import kotlin.io.path.exists
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes

object DesktopStartup {

    private fun getMainClzPath(): String {
        return try {
            val anchor = Main::class.java
            val uri: URI = anchor.protectionDomain.codeSource.location.toURI()
            var p: Path = Paths.get(uri)

            // JAR
            if (Files.isRegularFile(p)) p = p.parent

            p.toRealPath().toString() // resolve symlink?
        } catch (e: Exception) {
            throw RuntimeException("Unable to get Main class path", e)
        }
    }

    private fun getExecutablePath(): String {
        val path = ProcessHandle.current()
            .info()
            .command()
            .orElse(null)
        if (path == null)
            throw RuntimeException("Unable to get executable path via ProcessHandle.current()")
        return path
    }

    private fun startedFromWinDirError(path: String): Nothing {
        try {
            DefaultExceptionWindow(
                "Please start the app from its installation folder or use a Shortcut. \n\nUnsupported CWD: $path",
                null
            )
        } finally {
            exitProcess(1)
        }
    }

    private fun nonWritableCWDError(path: String): Nothing {
        try {
            DefaultExceptionWindow("App working directory is not writable: $path", null)
        } finally {
            exitProcess(1)
        }
    }

    private fun customCWDNotSupportedError(path: String): Nothing {
        try {
            DefaultExceptionWindow("Custom App CWD is currently not supported: $path", null)
        } finally {
            exitProcess(1)
        }
    }


    private fun prepareAWTThread() {
        val handler = Thread.UncaughtExceptionHandler { thread: Thread, thr: Throwable ->
            runCatching {
                Logger.tryLog { "Exception in EDT thread, '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
                val useExceptionWindow = tryLockExceptionWindow()
                if (!useExceptionWindow)
                    return@UncaughtExceptionHandler
                DefaultExceptionWindow("Exception in EDT thread, '${Thread.currentThread().name}': ", thr)
            }
            exitProcess(1)
        }
        try {
            SwingUtilities.invokeAndWait {
                val EDT = Thread.currentThread()
                    .apply { uncaughtExceptionHandler = handler }
                if (EDT.uncaughtExceptionHandler !== handler) {
                    handler.uncaughtException(Thread.currentThread(), IllegalStateException("Unable to Install UncaughtExceptionHandler on AWT EDT Thread, flag is not set"))
                }
            }
        } catch (t: Throwable) {
            handler.uncaughtException(Thread.currentThread(), IllegalStateException("Unable to Install UncaughtExceptionHandler on AWT EDT Thread, cause_msg=${t.message}", t))
        }
    }

    // pre-check kotlinx stuff used for initialization
    private fun prepareStdKtx() {
        try {
            runBlocking {
                GlobalScope.launch(Dispatchers.Default) { delay(1) }.join()
                CoroutineScope(SupervisorJob()).async(Dispatchers.IO) { delay(1) }.await()
            }
        } catch (e: Exception) {
            try {
                Logger.tryLog {"fail to prepareStdKtx: ${e.stackTraceToString()}"}
                DefaultExceptionWindow("fail to prepareStdKtx", e)
            } finally {
                exitProcess(1)
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun prepareUIFoundation() {
        try {
            runBlocking {
                launch(UiFoundation.mainImmediateUiDispatcher) {
                    UiFoundation.provideMainThread()
                }.join()
            }
        } catch (e: Exception) {
            try {
                Logger.tryLog {"fail to prepareUIFoundation: ${e.stackTraceToString()}"}
                DefaultExceptionWindow("fail to prepareUIFoundation", e)
            } finally {
                exitProcess(1)
            }
        }
    }


    private fun prepareNativeLibs() = runCatching {
        NativeLibsInitializer.init()
    }.onFailure { e ->
        Logger.tryLog {"fail to prepareNativeLibs: ${e.stackTraceToString()}"}
        DefaultExceptionWindow("fail to prepareNativeLibs", e)
        exitProcess(1)
    }


    private fun prepareGCRunner() {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            runCatching {
                while (isActive) {
                    delay(10.minutes)
                    Runtime.getRuntime().gc()
                }
            }.onFailure { thr ->
                if (isActive) {
                    var shutdown = true
                    runCatching {
                        Logger.tryLog { "Failure in GC runner, '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
                        SwingUtilities.invokeAndWait {
                            val useExceptionWindow = tryLockExceptionWindow()
                            if (!useExceptionWindow) {
                                shutdown = false
                                return@invokeAndWait
                            }
                            DefaultExceptionWindow("Failure in GC runner, '${Thread.currentThread().name}': ", thr)
                        }
                    }
                    if (shutdown)
                        exitProcess(1)
                }
            }
        }
    }


    private fun checkAppManifest() = runCatching {
        val manifestFile = jFile("${MLToolboxApp.Companion.DATA_DIR_PATH}\\AppManifest.json")
        if (!manifestFile.exists())
            throw RuntimeException("Missing AppManifest, app version is '${MLToolboxApp.Companion.RELEASE_VERSION}'")

        val readFile = runCatching {
            manifestFile.readText()
        }.fold(onSuccess = {it}, onFailure = {
            throw RuntimeException("Unable to read manifest file", it)
        })
        val parseJson = runCatching {
            Json.Default.parseToJsonElement(readFile)
        }.fold(onSuccess = {it}, onFailure = {
            throw RuntimeException("Unable to parse manifest file into json", it)
        })


        if (parseJson is JsonObject) {
            val appManifestJson = parseJson["dev.psiae.mltoolbox.AppManifest"]
            if (appManifestJson is JsonObject) {
                val manifestVersion = appManifestJson["manifestVersion"]?.castOrNull<JsonPrimitive>()?.contentOrNull
                val manifestType = appManifestJson["manifestType"]?.castOrNull<JsonPrimitive>()?.contentOrNull
                if (manifestVersion == "0.1.0-alpha.1" && manifestType == "AppManifest") {
                    val data = appManifestJson["data"]
                    if (data is JsonObject) {
                        val meta = data["meta"]
                        if (meta is JsonObject) {
                            val versioning = meta["versioning"]
                            if (versioning is JsonObject) {
                                val versionNumber = versioning["versionNumber"]?.castOrNull<JsonPrimitive>()?.contentOrNull
                                if (versionNumber != MLToolboxApp.version)
                                    error("AppManifest is invalid: version number is $versionNumber expected ${MLToolboxApp.version}")
                                return@runCatching
                            }
                        }
                    }
                }
            }
        }

        Logger.tryLog { parseJson.toString() }

        throw RuntimeException("AppManifest is invalid")
    }.onFailure { e ->
        Logger.tryLog {"checkAppManifest failed: ${e.stackTraceToString()}"}
        DefaultExceptionWindow("Fail to check app manifest", e)
        exitProcess(1)
    }

    operator fun invoke(
        args: Array<String>,
    )  {
        val userDir = System.getProperty("user.dir")?.ifEmpty { null }
            ?: throw RuntimeException("user.dir is empty: ${System.getProperty("user.dir")}")

        if (!Paths.get(userDir).exists())
            throw RuntimeException("user.dir does not exist: $userDir")

        // happens when you start the .exe from search bar
        if (Paths.get(userDir).startsWith("C:\\Windows\\"))
            startedFromWinDirError(userDir)

        if (!Files.isWritable(Paths.get(userDir)))
            nonWritableCWDError(userDir)

        // We don't allow custom CWD for now.
        if (!Paths.get(userDir).startsWith(Paths.get(AppInitializer.getExecutablePath()).parent))
            customCWDNotSupportedError(userDir)

        Logger.init(MLToolboxApp.logsDir.absolutePath)

        Logger.log("\n\n\n\n")
        Logger.log("~~~~~~~~~~~~~~~~~~~~")
        Logger.log("")
        Logger.log("MLToolbox.exe launched")
        Logger.log("args: ${args.contentToString()}")
        Logger.log("version: ${MLToolboxApp.RELEASE_VERSION}")
        Logger.log("")
        Logger.log("~~~~~~~~~~~~~~~~~~~~")
        Logger.log("\n\n")

        Logger.log("Timezone: ${TimeZone.currentSystemDefault()}")
        Logger.log("CWD: $userDir")
        Logger.log("EXE: ${getExecutablePath()}")
        Logger.log("CLZ: ${getMainClzPath()}")
        Logger.log("\n\n")


        checkAppManifest()

        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, thr: Throwable ->
            Logger.tryLog { "Exception in thread '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
            exitProcess(1)
        }

        prepareAWTThread()
        prepareStdKtx()
        prepareUIFoundation()

        runCatching {
            AppInitializer.init()
        }.onFailure { e ->
            try {
                Logger.tryLog {"AppInitializer init failed: ${e.stackTraceToString()}"}
                DefaultExceptionWindow("AppInitializer init failed", e)
            } finally {
                exitProcess(1)
            }
        }

        SingleInstance.lock()
        prepareGCRunner()
        prepareNativeLibs()
    }
}