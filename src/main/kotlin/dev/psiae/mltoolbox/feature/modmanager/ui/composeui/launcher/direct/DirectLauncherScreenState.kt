package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.launcher.direct

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.feature.modmanager.launcher.ui.LauncherScreenState
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.utils.deleteRecursivelyBool
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.Closeable
import java.io.IOException
import kotlin.io.path.ExperimentalPathApi

@Composable
fun rememberDirectLauncherScreenState(
    launcherScreenState: LauncherScreenState
): DirectLauncherScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(launcherScreenState) {
        DirectLauncherScreenState(launcherScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class DirectLauncherScreenState(
    val launcherScreenState: LauncherScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var selectedTab by mutableStateOf("direct")

    var directModsTab by mutableStateOf("ue4ss")

    var launchingModded by mutableStateOf(false)
        private set

    var launchingVanilla by mutableStateOf(false)
        private set

    val snackbarErrorChannel = Channel<String>(capacity = Channel.CONFLATED)

    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

    }

    fun userInputLaunchGame() {
        if (launchingModded)
            return
        launchingModded = true
        coroutineScope.launch(uiContext.dispatchContext.mainDispatcher.immediate) {

            var launched = false
            var notLaunchedErrMessage = "unknown_not_launched"
            withContext(Dispatchers.IO) {
                val process = runCatching {
                    launcherScreenState.modManagerScreenState.requireGameLauncherFile().absolutePath.let {
                        val processBuilder = ProcessBuilder("cmd.exe", "/c", "start", "", it)
                        processBuilder.start()
                    }
                }.onFailure { e ->
                    if (e is IOException) {
                        notLaunchedErrMessage = "Could not launch modded game (IO Error)"
                        return@withContext
                    }
                    throw e
                }
                launched = true
            }

            if (!launched) {
                snackbarErrorChannel.send("Error trying to launch vanilla:\n$notLaunchedErrMessage")
            }
        }.invokeOnCompletion {
            launchingModded = false
        }
    }

    @OptIn(ExperimentalPathApi::class)
    fun userInputLaunchVanilla() {
        if (launchingVanilla)
            return
        launchingVanilla = true
        coroutineScope.launch {

            var launched = false
            var notLaunchedErrMessage = "unknown_not_launched"
            withContext(Dispatchers.IO) {
                val gameFile = launcherScreenState.modManagerScreenState.requireGameBinaryFile()
                val gameFileDir = gameFile.parentFile

                val closeables = mutableListOf<Closeable>()
                val dwmApi = jFile("$gameFileDir\\dwmapi.dll")
                if (dwmApi.isFile) {
                    if (!dwmApi.delete()) {
                        notLaunchedErrMessage = "Could not delete ${gameFileDir.name}\\dwmapi.dll"
                        return@withContext
                    }
                }
                val ue4ssDir = jFile("$gameFileDir\\ue4ss")
                if (ue4ssDir.isDirectory) {
                    if (!ue4ssDir.toPath().deleteRecursivelyBool()) {
                        notLaunchedErrMessage = "Could not delete ${gameFileDir.name}\\ue4ss directory recursively"
                        return@withContext
                    }
                }

                val (unrealGameRoot, gameRoot) = resolveGameRoot(gameFile)
                val pakModsDir = jFile("$gameRoot\\Content\\Paks\\~mods")
                if (pakModsDir.exists()) {
                    if (!pakModsDir.toPath().deleteRecursivelyBool()) {
                        notLaunchedErrMessage = "Could not delete ${jFile(gameRoot).name}\\Content\\Paks\\~mods directory recursively"
                        return@withContext
                    }
                }

                val logicModsDir = jFile("$gameRoot\\Content\\Paks\\LogicMods")
                if (logicModsDir.exists()) {
                    if (!logicModsDir.toPath().deleteRecursivelyBool()) {
                        notLaunchedErrMessage = "Could not delete ${jFile(gameRoot).name}\\Content\\Paks\\LogicMods directory recursively"
                        return@withContext
                    }
                }

                val process = runCatching {
                    launcherScreenState.modManagerScreenState.requireGameBinaryFile().absolutePath.let {
                        val processBuilder = ProcessBuilder("cmd.exe", "/c", "start", "", it)
                        processBuilder.start()
                    }
                }.onFailure { e ->
                    if (e is IOException) {
                        notLaunchedErrMessage = "Could not start game process (IO Error)"
                        return@withContext
                    }
                    throw e
                }
                launched = true
            }

            if (!launched) {
                snackbarErrorChannel.send("Error trying to launch vanilla:\n$notLaunchedErrMessage")
            }
        }.invokeOnCompletion {
            launchingVanilla = false
        }
    }

    private fun resolveGameRoot(targetGameBinary: jFile): Pair<String, String> {
        if (!targetGameBinary.isFile)
            error("[resolveGameRoot]: targetGameBinary is not a file")
        return targetGameBinary.absolutePath
            .split("\\")
            .let { split ->
                if (split.size < 5) {
                    error("unable to find target game binary root directory, split size to small=${split.size}")
                }
                split.dropLast(4).joinToString("\\") to split.dropLast(3).joinToString("\\")
            }
    }
}