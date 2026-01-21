package dev.psiae.mltoolbox.shared.app

import dev.psiae.mltoolbox.core.LazyConstructor
import dev.psiae.mltoolbox.core.valueOrNull
import kotlinx.coroutines.SupervisorJob
import java.io.File

class MLToolboxApp {
    companion object {
        private val INSTANCE = LazyConstructor<MLToolboxApp>()
        private val EXE_PATH = LazyConstructor<String>()

        const val RELEASE_VERSION = "1.0.2-a.1"
        const val DEV_VERSION = "1.0.2-a.1-dev.20251221.1"
        const val IS_DEV = false

        val version = if (IS_DEV) DEV_VERSION else RELEASE_VERSION


        const val APP_ID = "dev.psiae.mltoolbox"
        const val DATA_DIR_PATH = "MLToolboxApp"


        val coroutineLifetimeJob = SupervisorJob()

        val appDir
            get() = File(DATA_DIR_PATH, "app")
        val tempDir
            get() = File(DATA_DIR_PATH, "temp")
        val logsDir
            get() = File(DATA_DIR_PATH, "logs")
        val dbDir
            get() = File(DATA_DIR_PATH, "db")
        val userDir
            get() = File(DATA_DIR_PATH, "user")
        val modManagerDir
            get() = File(DATA_DIR_PATH, "modmanager")
        val gameManagerDir
            get() = File(DATA_DIR_PATH, "gamemanager")

        val exePath: String
            get() = EXE_PATH.valueOrNull()
                ?: error("EXE_PATH not initialized")

        object Dirs {
            val temp
                get() = tempDir
            val logs
                get() = logsDir
            val gameManager
                get() = gameManagerDir
            val modManager
                get() = modManagerDir
        }

        fun construct() = INSTANCE.constructOrThrow(
            lazyValue = { MLToolboxApp() },
            lazyThrow = { error("MLToolboxApp already initialized") }
        )

        fun requireInstance(): MLToolboxApp = INSTANCE.valueOrNull()
            ?: error("MLToolboxApp not initialized")

        fun provideExePath(path: String) = EXE_PATH.constructOrThrow(
            lazyValue = { path },
            lazyThrow = { error("EXE_PATH already initialized") }
        )
    }
}