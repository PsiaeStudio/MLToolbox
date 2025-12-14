package dev.psiae.mltoolbox.feature.modmanager.ui.compose.setup

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGamePaths
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.orEmpty
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toPath
import dev.psiae.mltoolbox.foundation.fs.path.startsWith
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.domain.model.GamePlatform
import dev.psiae.mltoolbox.shared.domain.model.GameVersion
import dev.psiae.mltoolbox.shared.domain.model.ManorLordsGameVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException

@Composable
fun rememberSetupGameContextPanelState(
    setupScreenState: SetupModManagerScreenModel
): SetupGameContextPanelState {
    val state = remember(setupScreenState) { SetupGameContextPanelState(setupScreenState) }
    return state
}

class SetupGameContextPanelState(
    private val setupScreenState: SetupModManagerScreenModel
) {

    val coroutineScope
        get() = setupScreenState.coroutineScope

    val screenContext
        get() = setupScreenState.context

    val fs = FileSystem.SYSTEM
    val ioDispatcher = screenContext.dispatch.ioDispatcher

    var selectedGamePlatform by mutableStateOf<GamePlatform?>(null)
        private set

    var selectedGamePaths by mutableStateOf<ModManagerGamePaths?>(null)
        private set

    var selectedGameVersion by mutableStateOf<GameVersion?>(null)
        private set

    var alreadySelectedPlatform by mutableStateOf(false)
        private set
    var alreadySelectedGameInstallFolder by mutableStateOf(false)
        private set
    var alreadySelectedGamePaths by mutableStateOf(false)
        private set
    var alreadySelectedGameVersion by mutableStateOf(false)
        private set


    var pickedGameFolderErr: Boolean by mutableStateOf(false)
        private set

    var pickedGameFolderErrMsg: String by mutableStateOf("")
        private set


    val hasAllDirectoriesSelected by derivedStateOf {
        selectedGamePaths?.let { paths ->
            !paths.install.isEmpty && !paths.root.isEmpty && !paths.launcher.isEmpty && !paths.binary.isEmpty && !paths.paks.isEmpty
        } == true
    }

    val hasGameVersionSelected by derivedStateOf {
        (selectedGameVersion is ManorLordsGameVersion) and
                (selectedGameVersion !is ManorLordsGameVersion.Custom)
    }

    fun selectedGamePlatformOrThrow(): GamePlatform = checkNotNull(selectedGamePlatform) {
        "Selected game platform is null"
    }
    fun selectedGamePathsOrThrow(): ModManagerGamePaths = checkNotNull(selectedGamePaths) {
        "Selected game paths is null"
    }
    fun selectedGameVersionOrThrow(): GameVersion = checkNotNull(selectedGameVersion) {
        "Selected game version is null"
    }

    fun selectGamePlatform(gamePlatform: GamePlatform) {
        selectedGamePlatform = gamePlatform
        alreadySelectedPlatform = true
    }

    fun changeGamePlatform() {
        selectedGamePlatform = null
        alreadySelectedPlatform = false

        changeGameInstallFolder()
    }
    fun changeGameInstallFolder() {
        selectedGamePaths = null
        alreadySelectedGameInstallFolder = false

        changeGamePaths()
    }
    fun changeGamePaths() {
        alreadySelectedGamePaths = false

        changeGameVersion()
    }

    fun changeGameVersion() {
        selectedGameVersion = null
        alreadySelectedGameVersion = false
    }

    fun selectGameInstallFolder(path: Path) {
        selectedGamePaths = (selectedGamePaths ?: ModManagerGamePaths.EMPTY).copy(
            install = path
        )
        alreadySelectedGameInstallFolder = true
    }

    fun onBeginFolderPick() {
        pickedGameFolderErr = false
        pickedGameFolderErrMsg = ""
    }
    fun onGameInstallFolderPicked(folder: Path?) {
        if (folder == null)
            return

        if (isProcessingGamePickedFolder)
            return

        val folderPathString = folder.toString()
        if (folderPathString.startsWith("::")) {
            pickedGameFolderErr = true
            pickedGameFolderErrMsg = "Path not supported: $folderPathString"
            return
        }

        if (MLToolboxApp.exePath.toPath().startsWith(folder, true)) {
            pickedGameFolderErr = true
            pickedGameFolderErrMsg = "Game Install folder containing this app is not allowed"
            return
        }

        selectedGamePaths = ModManagerGamePaths.EMPTY.copy(
            install = folder
        )

        processGameFolderPicked(folder, selectedGamePlatformOrThrow())
    }

    var isProcessingGamePickedFolder by mutableStateOf(false)
        private set

    var processingPickedGameFolderStatusMsg by mutableStateOf("")
        private set

    fun processGameFolderPicked(
        folder: Path,
        selectedPlatform: GamePlatform
    ) {
        if (!isProcessingGamePickedFolder) {
            isProcessingGamePickedFolder = true
            coroutineScope.launch {
                var isError = false
                var errorMsg = ""
                var currentSelectedGamePaths = selectedGamePathsOrThrow()
                processingPickedGameFolderStatusMsg = "processing..."
                withContext(Dispatchers.IO) {
                    runCatching {
                        val rootFolderPath = when (selectedPlatform) {
                            GamePlatform.Steam, GamePlatform.EpicGamesStore, GamePlatform.GogCom -> folder
                            GamePlatform.XboxPcGamePass -> folder / "Content"
                        }.takeIf { path ->
                            fs.file(path).let { it.exists() && it.followLinks().isDirectory() }
                        }.orEmpty()

                        val launcherFilePath = when (selectedPlatform) {
                            GamePlatform.Steam, GamePlatform.EpicGamesStore, GamePlatform.GogCom -> folder / "ManorLords.exe"
                            GamePlatform.XboxPcGamePass -> folder / "Content\\gamelaunchhelper.exe"
                        }.takeIf { path ->
                            fs.file(path).let { it.exists() && it.followLinks().isRegularFile() }
                        }.orEmpty()

                        val gameBinaryExeFilePath = when (selectedPlatform) {
                            GamePlatform.Steam, GamePlatform.EpicGamesStore, GamePlatform.GogCom -> folder / "ManorLords\\Binaries\\Win64\\ManorLords-Win64-Shipping.exe"
                            GamePlatform.XboxPcGamePass -> folder / "Content\\ManorLords\\Binaries\\WinGDK\\ManorLords-WinGDK-Shipping.exe"
                        }.takeIf { path ->
                            fs.file(path).let { it.exists() && it.followLinks().isRegularFile() }
                        }.orEmpty()

                        val gamePaksFolder = when (selectedPlatform) {
                            GamePlatform.Steam, GamePlatform.EpicGamesStore, GamePlatform.GogCom -> folder / "ManorLords\\Content\\Paks\\"
                            GamePlatform.XboxPcGamePass -> folder / "Content\\ManorLords\\Content\\Paks\\"
                        }.takeIf { path ->
                            fs.file(path).let { it.exists() && it.followLinks().isDirectory() }
                        }.orEmpty()

                        currentSelectedGamePaths = currentSelectedGamePaths.copy(
                            root = rootFolderPath,
                            launcher = launcherFilePath,
                            binary = gameBinaryExeFilePath,
                            paks = gamePaksFolder
                        )
                    }.catchOrRethrow { e ->
                        Logger.tryLog { e.stackTraceToString() }
                        if (e is IOException) {
                            isError = true
                            errorMsg = "IO Error, see log for details"
                            return@withContext
                        }
                    }
                }
                selectedGamePaths = currentSelectedGamePaths
                if (isError) {
                    pickedGameFolderErr = true
                    pickedGameFolderErrMsg = errorMsg
                } else {
                    alreadySelectedGameInstallFolder = true
                }
                isProcessingGamePickedFolder = false
            }
        }
    }

    fun selectGameRootFolder(path: Path) {
        val paths = selectedGamePathsOrThrow()
        selectedGamePaths = paths.copy(
            root = path,
            launcher = if (!paths.launcher.startsWith(path)) Path.EMPTY else paths.launcher,
            binary = if (!paths.binary.startsWith(path))  Path.EMPTY else paths.binary,
            paks = if (!paths.paks.startsWith(path)) Path.EMPTY else paths.paks
        )
    }
    fun selectGameLauncherFile(path: Path) {
        val paths = selectedGamePathsOrThrow()
        selectedGamePaths = paths.copy(launcher = path)
    }
    fun selectGameBinaryFile(path: Path) {
        val paths = selectedGamePathsOrThrow()
        selectedGamePaths = paths.copy(binary = path)
    }
    fun selectGamePaksFolder(path: Path) {
        val paths = selectedGamePathsOrThrow()
        selectedGamePaths = paths.copy(paks = path)
    }

    fun confirmSelectPaths() {
       if (hasAllDirectoriesSelected)
            alreadySelectedGamePaths = true
    }

    fun selectGameVersion(gameVersion: GameVersion) {
        if (
            gameVersion !is ManorLordsGameVersion
        ) {
            return
        }
        selectedGameVersion = gameVersion
    }

    fun confirmSelectGameVersion() {
        if (hasGameVersionSelected) {
            alreadySelectedGameVersion = true

            setupScreenState.selectGameContext(
                ModManagerGameContext(
                    platform = selectedGamePlatformOrThrow(),
                    paths = selectedGamePathsOrThrow(),
                    version = selectedGameVersionOrThrow()
                )
            )
        }
    }
}