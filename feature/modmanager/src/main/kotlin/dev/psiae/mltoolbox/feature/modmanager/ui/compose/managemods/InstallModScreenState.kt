package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallUEPakModScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallUe4ssModScreen
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstallUe4ssScreen
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.ScreenState
import dev.psiae.mltoolbox.foundation.ui.nav.ScreenNavEntry
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun rememberInstallModScreenState(
    goBack: () -> Unit,
    gameContext: ModManagerGameContext,
    initializer: InstallModScreenState.() -> Unit
): InstallModScreenState {
    val latestGoBack by rememberUpdatedState(goBack)
    val screenContext = LocalScreenContext.current
    return remember {
        InstallModScreenState(
            screenContext,
            _goBack = {
                latestGoBack.invoke()
            },
            gameContext = gameContext,
        ).apply(initializer)
    }
}

class InstallModScreenState(
    context: ScreenContext,
    private val _goBack: () -> Unit,
    val gameContext: ModManagerGameContext
) : ScreenState(context) {

    val fs = FileSystem.SYSTEM

    val navigator = Navigator(this)

    var isDirect by mutableStateOf(false)
        private set

    var isManaged by mutableStateOf(false)
        private set

    var isReady by mutableStateOf(false)
        private set

    var isUe4ssInstalled by mutableStateOf(false)
        private set

    override fun onRemembered() {
        super.onRemembered()
        init()
    }

    private fun init() {
        coroutineScope.launch {
            isUe4ssInstalled = isUe4ssInstalled()
            launch {
                while (currentCoroutineContext().isActive) {
                    isUe4ssInstalled = isUe4ssInstalled()
                    delay(1000)
                }
            }

            isReady = true
        }
    }

    private suspend fun isUe4ssInstalled(): Boolean = withContext(context.dispatch.ioDispatcher) {
        runCatching {
            val binaryFile = gameContext.paths.binary.takeIf { fs.file(it).followLinks().isRegularFile() }
                ?: return@withContext false
            val binaryFolder = binaryFile.parent?.takeIf { fs.file(it).followLinks().isDirectory() }
                ?: return@withContext false
            val proxyDll = binaryFolder.resolve("dwmapi.dll").takeIf { fs.file(it).followLinks().isRegularFile() }
                ?: return@withContext false
            val ue4ssFolder = binaryFolder.resolve("ue4ss").takeIf { fs.file(it).followLinks().isDirectory() }
                ?: return@withContext false
            val ue4ssDll = ue4ssFolder.resolve("UE4SS.dll").takeIf { fs.file(it).followLinks().isRegularFile() }
                ?: return@withContext false
            val modsFolder = ue4ssFolder.resolve("Mods").takeIf { fs.file(it).followLinks().isDirectory() }
                ?: return@withContext false
        }.catchOrRethrow { e ->
            when (e) {
                is IOException -> return@withContext false
            }
        }
        true
    }

    fun setDirectInstall() {
        isDirect = true
    }

    fun installUe4ssModLoader() {
        navigator.navigateToInstallUe4ssModLoader()
    }

    fun installUe4ssModLoaderMod() {
        navigator.navigateToInstallUe4ssMod()
    }

    fun installUe4PakModLoaderMod() {
        navigator.navigateToInstallUEPakMod()
    }

    fun goBackFromScreen(
        entry: ScreenNavEntry
    ) {
        navigator.stack.removeIf { it.id == entry.id }
    }

    fun exitScreen() {
        _goBack()
    }

    class Navigator(
        val screen: InstallModScreenState
    ) {
        val stack = mutableStateListOf<ScreenNavEntry>()

        fun navigateToInstallUe4ssModLoader() {
            stack.clear()
            stack.add(ScreenNavEntry(InstallUe4ssScreen))
        }

        fun navigateToInstallUe4ssMod() {
            stack.clear()
            stack.add(ScreenNavEntry(InstallUe4ssModScreen))
        }

        fun navigateToInstallUEPakMod() {
            stack.clear()
            stack.add(ScreenNavEntry(InstallUEPakModScreen))
        }
    }
}