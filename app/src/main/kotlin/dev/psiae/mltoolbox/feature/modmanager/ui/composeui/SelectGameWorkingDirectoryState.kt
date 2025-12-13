package dev.psiae.mltoolbox.feature.modmanager.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.optionals.getOrNull

@Composable
fun rememberSelectGameWorkingDirectoryState(
    modManagerScreenState: ModManagerScreenState
): SelectGameWorkingDirectoryState {
    val uiContext = LocalScreenContext.current
    val state = remember(modManagerScreenState) {
        SelectGameWorkingDirectoryState(modManagerScreenState, uiContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class SelectGameWorkingDirectoryState(
    private val modManagerScreenState: ModManagerScreenState,
    private val uiContext: ScreenContext
) {

    private val lifetime = SupervisorJob()

    private var _coroutineScope: CoroutineScope? = null

    val coroutineScope: CoroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var chosenDir: jFile? by mutableStateOf(null)
        private set

    var openFolderPicker by mutableStateOf(false)
        private set

    var searchingManorLordsProcess by mutableStateOf(false)
        private set

    var traverseRunningInstanceNotFound by mutableStateOf(false)
        private set

    var selectedPlatform by mutableStateOf("")

    var alreadySelectedPlatform by mutableStateOf(false)
        private set

    var pickedGameFolder: jFile? by mutableStateOf(null)
        private set

    var pickedGameFolderErr: Boolean by mutableStateOf(false)
        private set

    var pickedGameFolderErrMsg: String by mutableStateOf("")
        private set

    var gameRootFolder: jFile? by mutableStateOf(null)
        private set

    var alreadySelectedGameFolder by mutableStateOf(false)
        private set

    var alreadySelectedGameDirs by mutableStateOf(false)
        private set

    var processingPickedFolder by mutableStateOf(false)
        private set

    var processingPickedFolderStatusMsg by mutableStateOf("")
        private set

    var processingPickedFolderErr by mutableStateOf(false)
        private set

    var processingPickedFolderErrMsg by mutableStateOf("")
        private set

    var processingFilePicked by mutableStateOf(false)
        private set

    var gameLauncherExeFile by mutableStateOf<jFile?>(null)
        private set
    var gameBinaryExeFile by mutableStateOf<jFile?>(null)
        private set
    var gamePaksFolder by mutableStateOf<jFile?>(null)
        private set

    val hasAllDirectoriesSelected by derivedStateOf {
        gameRootFolder != null && gameLauncherExeFile != null && gameBinaryExeFile != null && gamePaksFolder != null
    }

    var selectedGameVersion by mutableStateOf("")
        private set

    var selectedGameVersionCustom by mutableStateOf("")
        private set

    val hasGameVersionSelected by derivedStateOf {
        if (selectedGameVersion == "custom")
            return@derivedStateOf selectedGameVersionCustom.isNotBlank()
        selectedGameVersion.isNotBlank()
    }

    private var processingUserSelectGameDirs = false

    fun stateEnter() {
        _coroutineScope = uiContext.createCoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

    }

    fun changeGameVersion(version: String) {
        selectedGameVersion = version
    }
    fun changeGameVersionCustom(version: String) {
        selectedGameVersionCustom = version
    }

    var alreadySelectedGameVersion by mutableStateOf(false)
        private set

    fun changePlatform() {
        selectedPlatform = ""
        alreadySelectedPlatform = false

        changeFolder()
    }
    fun changeFolder() {
        pickedGameFolder = null
        alreadySelectedGameFolder = false


        changeDirectories(true)
    }
    fun changeDirectories(
        clear: Boolean = false
    ) {
        if (clear) {
            gameRootFolder = null
            gameLauncherExeFile = null
            gameBinaryExeFile = null
            gamePaksFolder = null
        }

        alreadySelectedGameDirs = false

        changeGameVersion()
    }
    fun changeGameVersion() {
        selectedGameVersion = ""
        selectedGameVersionCustom = ""
        alreadySelectedGameVersion = false
    }


    fun commitSelectPlatform() {
        if (selectedPlatform.isNotEmpty())
            alreadySelectedPlatform = true
    }

    fun commitSelectGameFolder(gameFolder: String) {
        if (pickedGameFolder != null)
            alreadySelectedGameFolder = true
    }

    fun processFolderPicked() {
        val picked = pickedGameFolder
        if (picked != null && !processingPickedFolder) {
            processingPickedFolder = true
            modManagerScreenState.coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    processingPickedFolderStatusMsg = "processing..."
                    val folder = picked

                    when (selectedPlatform) {
                        "steam", "epic_games_store", "gog_com" -> {
                            val rootFolder = folder
                            if (rootFolder.exists() && rootFolder.isDirectory) {
                                gameRootFolder = rootFolder
                            }
                        }
                        "xbox_pc_gamepass" -> {
                            val rootFolder = jFile(folder, "Content")
                            if (rootFolder.exists() && rootFolder.isDirectory) {
                                gameRootFolder = rootFolder
                            }
                        }
                        else -> throw RuntimeException("Unknown selected platform: $selectedPlatform")
                    }

                    when (selectedPlatform) {
                        "steam", "epic_games_store", "gog_com" -> {
                            val launcherFile = jFile(folder, "ManorLords.exe")
                            if (launcherFile.exists() && launcherFile.isFile) {
                                gameLauncherExeFile = launcherFile
                            }
                        }
                        "xbox_pc_gamepass" -> {
                            val launcherFile = jFile(folder, "Content\\gamelaunchhelper.exe")
                            if (launcherFile.exists() && launcherFile.isFile) {
                                gameLauncherExeFile = launcherFile
                            }
                        }
                        else -> throw RuntimeException("Unknown selected platform: $selectedPlatform")
                    }

                    when (selectedPlatform) {
                        "steam", "epic_games_store", "gog_com" -> {
                            val binaryFile = jFile(folder, "ManorLords\\Binaries\\Win64\\ManorLords-Win64-Shipping.exe")
                            if (binaryFile.exists() && binaryFile.isFile) {
                                gameBinaryExeFile = binaryFile
                            }
                        }
                        "xbox_pc_gamepass" -> {
                            val binaryFile = jFile(folder, "Content\\ManorLords\\Binaries\\WinGDK\\ManorLords-WinGDK-Shipping.exe")
                            if (binaryFile.exists() && binaryFile.isFile) {
                                gameBinaryExeFile = binaryFile
                            }
                        }
                        else -> throw RuntimeException("Unknown selected platform: $selectedPlatform")
                    }

                    when (selectedPlatform) {
                        "steam", "epic_games_store", "gog_com" -> {
                            val paksFolder = jFile(folder, "ManorLords\\Content\\Paks\\")
                            if (paksFolder.exists() && paksFolder.isDirectory) {
                                gamePaksFolder = paksFolder
                            }
                        }
                        "xbox_pc_gamepass" -> {
                            val paksFolder = jFile(folder, "Content\\ManorLords\\Content\\Paks\\")
                            if (paksFolder.exists() && paksFolder.isDirectory) {
                                gamePaksFolder = paksFolder
                            }
                        }
                        else -> throw RuntimeException("Unknown selected platform: $selectedPlatform")
                    }
                }
                processingPickedFolder = false
            }
        }

    }


    fun filePick(pickedJFile: jFile?) {
        openFolderPicker = false

        if (pickedJFile == null)
            return

        if (jFile(MLToolboxApp.exePath).startsWith(pickedJFile)) {
            pickedGameFolderErr = true
            pickedGameFolderErrMsg = "Game Install folder containing this app is not allowed"
            return
        }

        pickedGameFolder = pickedJFile
        alreadySelectedGameFolder = true

        processFolderPicked()
    }

    fun userInputOpenGameDirPicker() {
        traverseRunningInstanceNotFound = false

        pickedGameFolderErr = false
        pickedGameFolderErrMsg = ""

        openFolderPicker = true
    }

    fun userInputFindFromRunningGameInstance() {
        if (openFolderPicker || searchingManorLordsProcess) return
        traverseRunningInstanceNotFound = false
        searchingManorLordsProcess = true
        modManagerScreenState.coroutineScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    queryProcessByExecName("ManorLords-Win64-Shipping.exe")
                        .ifEmpty { queryProcessByExecName("ManorLords-WinGDK-Shipping.exe") }
                        .ifEmpty { null }
                        ?.let {
                            modManagerScreenState.coroutineUIPublication {
                                chosenDir = jFile(it.first().path)
                                filePick(chosenDir)
                            }
                        }
                        ?: run {
                            traverseRunningInstanceNotFound = true
                        }
                }
            } finally {
                searchingManorLordsProcess = false
            }
        }
    }

    fun allowUserFileInput(): Boolean {
        return !openFolderPicker && !searchingManorLordsProcess
    }

    fun userSelectGameDirs() {
        if (hasAllDirectoriesSelected) {
            if (processingUserSelectGameDirs)
                return
            processingUserSelectGameDirs = true
            coroutineScope.launch {
                alreadySelectedGameDirs = true
                processingUserSelectGameDirs = false
            }
        }
    }

    fun userSelectGameVersion() {
        if (hasGameVersionSelected) {
            alreadySelectedGameVersion = true
            if (alreadySelectedPlatform && alreadySelectedGameFolder && alreadySelectedGameDirs && alreadySelectedGameVersion) {
                modManagerScreenState.chosenGameSettings(
                    selectedPlatform,
                    pickedGameFolder!!,
                    gameRootFolder!!,
                    gameLauncherExeFile!!,
                    gameBinaryExeFile!!,
                    gamePaksFolder!!,
                    selectedGameVersion,
                    selectedGameVersionCustom
                )
            }
        }
    }

    fun requirePickedFolder(): jFile {
        return requireNotNull(pickedGameFolder) {
            "pickedGameFolder was null"
        }
    }

    fun userPickGameRootFolder(file: jFile) {
        gameRootFolder = file
        val gameRootFolder = gameRootFolder!!
        if (gameLauncherExeFile?.startsWith(gameRootFolder) != true) {
            gameLauncherExeFile = null
        }
        if (gameBinaryExeFile?.startsWith(gameRootFolder) != true) {
            gameBinaryExeFile = null
        }
        if (gamePaksFolder?.startsWith(gameRootFolder) != true) {
            gamePaksFolder = null
        }
    }
    fun userPickGameLauncherExe(file: jFile) {
        gameLauncherExeFile = file
    }
    fun userPickGameBinaryExe(file: jFile) {
        gameBinaryExeFile = file
    }
    fun userPickGamePaksFolder(file: jFile) {
        gamePaksFolder = file
    }

    private class WindowProcessInfo(
        val name: String,
        val path: String,
        val id: Long,
    )

    private fun queryProcessByExecName(name: String): List<WindowProcessInfo> {
        val result = mutableListOf<WindowProcessInfo>()
        jProcessHandle.allProcesses().forEach { proc ->
            val exec = proc.info()?.command()?.getOrNull()
                ?: return@forEach
            val execName = exec.takeLastWhile { it != '\\' }
            if (execName != name) return@forEach
            val pid = proc.pid() ?: return@forEach
            result.add(
                WindowProcessInfo(
                    name = execName,
                    path = exec,
                    id = pid
                )
            )
        }
        return result
    }
}