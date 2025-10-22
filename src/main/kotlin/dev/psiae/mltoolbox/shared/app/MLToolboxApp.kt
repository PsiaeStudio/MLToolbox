package dev.psiae.mltoolbox.shared.app

import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.modmanager.ModManager
import dev.psiae.mltoolbox.shared.utils.LazyConstructor
import dev.psiae.mltoolbox.shared.utils.valueOrNull
import kotlinx.coroutines.SupervisorJob

class MLToolboxApp {
    val modManager = ModManager()

    companion object {
        private val INSTANCE = LazyConstructor<MLToolboxApp>()
        private val EXE_PATH = LazyConstructor<String>()

        const val RELEASE_VERSION = "1.0.1-alpha.4"
        const val APP_ID = "dev.psiae.mltoolbox"
        const val APP_DIR = "MLToolboxApp"

        const val IS_DEV = true

        val coroutineLifetimeJob = SupervisorJob()

        val tempDir
            get() = jFile(APP_DIR, "temp")
        val logsDir
            get() = jFile(APP_DIR, "logs")
        val dbDir
            get() = jFile(APP_DIR, "db")
        val userDir
            get() = jFile(APP_DIR, "user")
        val usersDir
            get() = jFile(APP_DIR, "users")
        val modManagerDir
            get() = jFile(APP_DIR, "modmanager")
        val gameManagerDir
            get() = jFile(APP_DIR, "gamemanager")

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

        fun getExePath() = EXE_PATH.valueOrNull()
            ?: error("EXE_PATH not initialized")
    }
}