package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.feature.modmanager.launcher.ManorLordsVanillaLauncher
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.shared.utils.isNullOrNotActive
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.File

@Composable
fun rememberModManagerScreenState(
    modManagerComposeState: ModManagerComposeState
): ModManagerScreenState {
    val composeUIContext = LocalScreenContext.current
    val state = remember(modManagerComposeState) {
        ModManagerScreenState(modManagerComposeState, composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class ModManagerScreenState(
    val modManagerComposeState: ModManagerComposeState,
    val uiContext: ScreenContext
) {
    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    val immediateDispatcher
        get() = uiContext.dispatch.mainDispatcher.immediate

    var gamePlatform by mutableStateOf("")
        private set
    var gameInstallDirectory by mutableStateOf<jFile?>(null)
        private set
    var gameRootDirectory by mutableStateOf<jFile?>(null)
        private set
    var gameLauncherFile by mutableStateOf<jFile?>(null)
        private set

    var gameBinaryFile by mutableStateOf<jFile?>(null)
        private set
    var gamePaksFolder by mutableStateOf<jFile?>(null)
        private set
    var gameVersion by mutableStateOf<String>("")
        private set
    var gameVersionCustom by mutableStateOf<String>("")
        private set

    val hasGameWorkingDirectory by derivedStateOf { gameBinaryFile != null }

    var changingWorkDir by mutableStateOf(false)
        private set

    var installUE4SS by mutableStateOf(false)
        private set

    var installUE4SSMod by mutableStateOf(false)
        private set

    var launchingGame by mutableStateOf(false)
        private set

    var ue4ssInstallationCheckWorker: Job? = null
        private set

    var checkingUE4SSInstallation by mutableStateOf(false)
        private set

    var isUE4SSNotInstalled by mutableStateOf(false)
        private set

    var ue4ssNotInstalledMessage by mutableStateOf<String?>(null)
        private set

    var checkingUE4SSInstallationStatusMessage by mutableStateOf<String?>(null)
        private set

    var refreshDashboardWorker: Job? = null
        private set

    var currentDrawerDestination by mutableStateOf("dashboard")


    fun stateEnter() {
        _coroutineScope = uiContext.createCoroutineScope()

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {
        if (gameBinaryFile != null) {
            refreshDashboard()
        }
    }

    suspend fun coroutineUIPublication(block: suspend () -> Unit) {
        withContext(uiContext.dispatch.mainDispatcher.immediate) {
            block()
        }
    }

    fun chosenGameSettings(
        platform: String,
        installDir: jFile,
        rootDir: jFile,
        launcherFile: jFile,
        binaryFile: jFile,
        paksFolder: jFile,
        gameVersion: String,
        gameVersionCustom: String
    ) {
        this.gamePlatform = platform
        this.gameInstallDirectory = installDir
        this.gameRootDirectory = rootDir
        this.gameLauncherFile = launcherFile
        this.gameBinaryFile = binaryFile
        this.gamePaksFolder = paksFolder
        this.gameVersion = gameVersion
        this.gameVersionCustom = gameVersionCustom
        changingWorkDir = false
    }

    private fun onChosenGameBinaryFile(file: File?) {
        refreshDashboard()
    }

    fun runOnUiContext(block: () -> Unit) {
        coroutineScope.launch(uiContext.dispatch.mainDispatcher.immediate) { block() }
    }

    fun userInputChangeWorkingDir() {
        changingWorkDir = true
    }

    fun userInputInstallUE4SS() {
        installUE4SS = true
    }

    fun installUE4SSExit() {
        installUE4SS = false

        refreshDashboard()
    }

    fun userInputInstallUE4SSMod() {
        installUE4SSMod = true
    }

    fun userInputInstallUE4SSModExit() {
        installUE4SSMod = false

        refreshDashboard()
    }

    fun launchGame() {
        coroutineScope.launch(uiContext.dispatch.mainDispatcher.immediate) {
            if (launchingGame) return@launch
            launchingGame = true
            withContext(Dispatchers.IO) {
                requireGameBinaryFile().absolutePath?.let {
                    Runtime.getRuntime().exec(it)
                }
            }
            launchingGame = false
        }
    }

    fun launchGameVanilla() {
        ManorLordsVanillaLauncher(coroutineScope, requireGameBinaryFile())
            .apply {
                launch()
            }
    }

    fun userInputRetryCheckUE4SSInstalled() {
        if (ue4ssInstallationCheckWorker.isNullOrNotActive())
            inputCheckUE4SSInstalled()
    }

    fun inputCheckUE4SSInstalled() {
        ue4ssInstallationCheckWorker?.cancel()
        ue4ssInstallationCheckWorker = coroutineScope.launch {
            doCheckUE4SSInstalled()
        }
    }

    fun requireGameLauncherFile(): jFile {
        return checkNotNull(gameLauncherFile) {
            "ModManager: gameLauncherFile not provided"
        }
    }
    fun requireGameBinaryFile(): jFile {
        return checkNotNull(gameBinaryFile) {
            "ModManager: gameBinaryFile not provided"
        }
    }

    fun refreshDashboard() {
        val last = refreshDashboardWorker?.apply { cancel() }
        refreshDashboardWorker = coroutineScope.launch {
            last?.apply {
                try { cancelAndJoin() } catch (_: CancellationException) {}
            }
            ue4ssInstallationCheckWorker?.apply {
                try { cancelAndJoin() } catch (_: CancellationException) {}
            }
            // TODO: redo changingWorkDir
            snapshotFlow { changingWorkDir }.first { !it }
            ue4ssInstallationCheckWorker?.apply {
                try { cancelAndJoin() } catch (_: CancellationException) {}
            }
            inputCheckUE4SSInstalled()
            checkingUE4SSInstallation = false
        }
    }

    private suspend fun doCheckUE4SSInstalled() {
        checkingUE4SSInstallation = true
        checkingUE4SSInstallationStatusMessage = "Checking UE4SS Installation ..."

        ue4ssNotInstalledMessage = null
        val workingDir = requireGameBinaryFile().parentFile
            ?: error("[ModManagerScreenState]: missing workingDir")
        withContext(Dispatchers.IO) {
            val dwmApi = jFile("$workingDir\\dwmapi.dll")
            if (!dwmApi.exists() || !dwmApi.isFile) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing dwmapi.dll"
                }
                return@withContext
            }
            val ue4ssFolder = jFile("$workingDir\\ue4ss")
            if (!ue4ssFolder.exists() || !ue4ssFolder.isDirectory) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss directory"
                }
                return@withContext
            }
            val ue4ssDll = jFile("$workingDir\\ue4ss\\ue4ss.dll")
            if (!ue4ssDll.exists() || !ue4ssDll.isFile) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss\\ue4ss.dll"
                }
                return@withContext
            }
            val modsFolder = jFile("$workingDir\\ue4ss\\Mods")
            if (!modsFolder.exists() || !modsFolder.isDirectory) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss\\Mods\\ directory"
                }
                return@withContext
            }
            val modsJson = jFile("$workingDir\\ue4ss\\Mods\\mods.json")
            if (!modsJson.exists() || !modsJson.isFile) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss\\Mods\\mods.json"
                }
                return@withContext
            }
            isUE4SSNotInstalled = false
        }
        checkingUE4SSInstallation = false
        checkingUE4SSInstallationStatusMessage = null
    }
}