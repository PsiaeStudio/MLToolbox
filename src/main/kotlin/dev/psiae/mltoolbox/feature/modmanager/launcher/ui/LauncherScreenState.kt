package dev.psiae.mltoolbox.feature.modmanager.launcher.ui

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.feature.modmanager.ui.composeui.ModManagerScreenState
import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.logger.Logger
import dev.psiae.mltoolbox.shared.modmanager.ModManager
import dev.psiae.mltoolbox.shared.utils.deleteRecursivelyBool
import dev.psiae.mltoolbox.shared.utils.removePrefix
import dev.psiae.mltoolbox.shared.utils.runtimeError
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.walk
import kotlin.uuid.Uuid

@Composable
fun rememberLauncherScreenState(
    modManagerScreenState: ModManagerScreenState
): LauncherScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(modManagerScreenState) {
        LauncherScreenState(modManagerScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class LauncherScreenState(
    val modManagerScreenState: ModManagerScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var selectedTab by mutableStateOf("direct")


    var launching by mutableStateOf<Boolean>(false)
        private set

    var launchingStatusMsg by mutableStateOf<String>("")
        private set

    var launchingErr by mutableStateOf<Boolean>(false)
        private set

    var launchingErrMsg by mutableStateOf<String>("")
        private set

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

    @OptIn(ExperimentalPathApi::class)
    fun userInputLaunchGame() {
        if (launching) return
        val selectedPlatform = modManagerScreenState.gamePlatform
        val gameInstallFolder = modManagerScreenState.gameInstallDirectory ?: return
        val gameRootFolder = modManagerScreenState.gameRootDirectory ?: return
        val gameLauncherFile = modManagerScreenState.gameLauncherFile ?: return
        val gameBinaryFile = modManagerScreenState.gameBinaryFile ?: return
        val gamePaksFolder = modManagerScreenState.gamePaksFolder ?: return
        val gameVersion = modManagerScreenState.gameVersion.ifBlank { null } ?: return
        launching = true
        launchingErr = false
        launchingErrMsg = ""
        coroutineScope.launch {
            launchingStatusMsg = "Launching ..."
            runCatching {
                val exe = when (selectedPlatform) {
                    "steam", "epic_games_store", "gog_com" -> {
                        if (gameVersion == "0.8.029a")
                            gameBinaryFile
                        else
                            gameLauncherFile
                    }
                    "xbox_pc_gamepass" -> gameLauncherFile
                    else -> error("Unknown platform: $selectedPlatform")
                }
                withContext(Dispatchers.IO) {
                    val processBuilder = ProcessBuilder("cmd.exe", "/c", "start", "", exe.absolutePath)
                    val process = processBuilder.start()
                }
            }.onFailure { t ->
                if (t is IOException) {
                    launchingErr = true
                    launchingErrMsg = "Could not start game process (IO Error)"
                    return@launch
                }
                throw t
            }
            launching = false
        }
        /*coroutineScope.launch {
            launchingStatusMsg = "Verifying..."
            delay(200)
            launchingStatusMsg = "Staging..."
            delay(200)
            launchingStatusMsg = "Deploying..."
            withContext(Dispatchers.IO) {
                if (!ModManager.stagingDir.mkdirs() && !ModManager.stagingDir.exists()) {
                    launchingErr = true
                    launchingErrMsg = "Failed to mkdirs into Staging folder"
                }
                if (!ModManager.deployDir.mkdirs() && !ModManager.deployDir.exists()) {
                    launchingErr = true
                    launchingErrMsg = "Failed to mkdirs into Deploys folder"
                }
                val dir = jFile(ModManager.deployDir, Uuid.generateV7().toString())
                if (!dir.mkdir()) {
                    launchingErr = true
                    launchingErrMsg = "Failed to mkdir for deploy $dir"
                }
                gameRootFolder.toPath().walk().forEach { path ->
                    val file = path.toFile()
                    val target = jFile(dir, file.absolutePath.removePrefix(gameRootFolder.absolutePath))
                    target.parentFile.mkdirs()
                    if (file.isFile)
                        Files.createSymbolicLink(target.toPath(), file.toPath())
                    else if (file.isDirectory)
                        Files.createDirectory(target.toPath())
                    else
                        runtimeError("missing target file: $file")
                }

                if (selectedPlatform == "xbox_pc_gamepass") {
                    jFile(
                        dir,
                        gameBinaryFile.absolutePath.removePrefix(gameRootFolder.absolutePath)
                    ).toPath().deleteIfExists()
                    val rename = gameBinaryFile.renameTo(
                        jFile(
                            dir,
                            gameBinaryFile.absolutePath.removePrefix(gameRootFolder.absolutePath)
                        )
                    )
                    jFile(
                        dir,
                        gameBinaryFile.absolutePath.removePrefix(gameRootFolder.absolutePath)
                    ).copyTo(gameBinaryFile)
                } else {
                    gameBinaryFile.copyTo(
                        jFile(
                            dir,
                            gameBinaryFile.absolutePath.removePrefix(gameRootFolder.absolutePath)
                        ),
                        overwrite = true
                    )
                }

                gameLauncherFile.copyTo(
                    jFile(
                        dir,
                        gameLauncherFile.absolutePath.removePrefix(gameRootFolder.absolutePath)
                    ),
                    overwrite = true
                )
            }
            launching = false
        }*/
    }

    @OptIn(ExperimentalPathApi::class)
    fun userInputLaunchVanillaGame() {
        if (launching) return
        val selectedPlatform = modManagerScreenState.gamePlatform
        val gameInstallFolder = modManagerScreenState.gameInstallDirectory ?: return
        val gameRootFolder = modManagerScreenState.gameRootDirectory ?: return
        val gameLauncherFile = modManagerScreenState.gameLauncherFile ?: return
        val gameBinaryFile = modManagerScreenState.gameBinaryFile ?: return
        val gamePaksFolder = modManagerScreenState.gamePaksFolder ?: return
        val gameVersion = modManagerScreenState.gameVersion.ifBlank { null } ?: return
        launching = true
        launchingErr = false
        launchingErrMsg = ""
        coroutineScope.launch {
            launchingStatusMsg = "Launching ..."
            withContext(Dispatchers.IO) {
                val gameFile = gameBinaryFile
                val gameFileDir = gameFile.parentFile

                val closeables = mutableListOf<Closeable>()
                val dwmApi = jFile("$gameFileDir\\dwmapi.dll")
                if (dwmApi.isFile) {
                    if (!dwmApi.delete()) {
                        launchingErr = true
                        launchingErrMsg = "Could not delete ${gameFileDir.name}\\dwmapi.dll"
                        return@withContext
                    }
                }
                val ue4ssDir = jFile("$gameFileDir\\ue4ss")
                if (ue4ssDir.isDirectory) {
                    if (!ue4ssDir.toPath().deleteRecursivelyBool()) {
                        launchingErr = true
                        launchingErrMsg = "Could not delete ${gameFileDir.name}\\ue4ss directory recursively"
                        return@withContext
                    }
                }

                val gameRoot = gameRootFolder.absolutePath
                val pakModsDir = jFile("$gamePaksFolder\\~mods")
                if (pakModsDir.isDirectory) {
                    if (!pakModsDir.toPath().deleteRecursivelyBool()) {
                        launchingErr = true
                        launchingErrMsg = "Could not delete ${jFile(gameRoot).name}\\Content\\Paks\\~mods directory recursively"
                        return@withContext
                    }
                }

                val logicModsDir = jFile("$gamePaksFolder\\LogicMods")
                if (logicModsDir.isDirectory) {
                    if (!logicModsDir.toPath().deleteRecursivelyBool()) {
                        launchingErr = true
                        launchingErrMsg = "Could not delete ${jFile(gameRoot).name}\\Content\\Paks\\LogicMods directory recursively"
                        return@withContext
                    }
                }

                val process = runCatching {
                    val exe = when (selectedPlatform) {
                        "steam", "epic_games_store", "gog_com" -> {
                            if (gameVersion == "0.8.029a")
                                gameBinaryFile
                            else
                                gameLauncherFile
                        }
                        "xbox_pc_gamepass" -> gameLauncherFile
                        else -> error("Unknown platform: $selectedPlatform")
                    }
                    val processBuilder = ProcessBuilder("cmd.exe", "/c", "start", "", exe.absolutePath)
                    processBuilder.start()
                }.onFailure { e ->
                    if (e is IOException) {
                        launchingErr = true
                        launchingErrMsg = "Could not start game process (IO Error)"
                        return@withContext
                    }
                    throw e
                }
            }
            launching = false
        }
    }
}