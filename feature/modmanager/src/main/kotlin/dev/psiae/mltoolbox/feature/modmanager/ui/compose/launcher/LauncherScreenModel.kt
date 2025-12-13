package dev.psiae.mltoolbox.feature.modmanager.ui.compose.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenModel
import dev.psiae.mltoolbox.feature.modmanager.ui.compose.ModManagerScreenState
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.foundation.ui.ScreenModel
import dev.psiae.mltoolbox.foundation.ui.compose.screen.rememberScreenModel
import dev.psiae.mltoolbox.shared.domain.model.GamePlatform
import dev.psiae.mltoolbox.shared.domain.model.ManorLordsGameVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun rememberLauncherScreenModel(
    modManagerScreenState: ModManagerScreenState
): LauncherScreenModel {
    val screenContext = LocalScreenContext.current
    return rememberScreenModel(modManagerScreenState) { LauncherScreenModel(modManagerScreenState, screenContext) }
}

class LauncherScreenModel(
    val modManagerScreenModel: ModManagerScreenState,
    screenContext: ScreenContext
) : ScreenModel(screenContext) {
    val fs = FileSystem.SYSTEM
    var selectedTab by mutableStateOf("direct")


    var isLaunching by mutableStateOf<Boolean>(false)
        private set

    var launchingStatusMsg by mutableStateOf<String>("")
        private set

    var launchingErr by mutableStateOf<Boolean>(false)
        private set

    var launchingErrMsg by mutableStateOf<String>("")
        private set

    fun init() {

    }

    fun userInputLaunchGame() {
        if (isLaunching) return
        val selectedPlatform = modManagerScreenModel.gameContext?.platform ?: return
        val gameInstallFolder = modManagerScreenModel.gameContext?.paths?.install ?: return
        val gameRootFolder = modManagerScreenModel.gameContext?.paths?.root ?: return
        val gameLauncherFile = modManagerScreenModel.gameContext?.paths?.launcher ?: return
        val gameBinaryFile = modManagerScreenModel.gameContext?.paths?.binary ?: return
        val gamePaksFolder = modManagerScreenModel.gameContext?.paths?.paks ?: return
        val gameVersion = modManagerScreenModel.gameContext?.version as? ManorLordsGameVersion ?: return
        isLaunching = true
        launchingErr = false
        launchingErrMsg = ""
        coroutineScope.launch {
            launchingStatusMsg = "Launching ..."
            runCatching {
                val exe = when (selectedPlatform) {
                    GamePlatform.Steam, GamePlatform.EpicGamesStore, GamePlatform.GogCom -> {
                        if (gameVersion == ManorLordsGameVersion.V_0_8_029a)
                            gameBinaryFile
                        else
                            gameLauncherFile
                    }
                    GamePlatform.XboxPcGamePass -> gameLauncherFile
                }
                withContext(Dispatchers.IO) {
                    val absolutePath = fs.absolute(exe)
                    val processBuilder = ProcessBuilder("cmd.exe", "/c", "start", "", absolutePath.toString())
                    val process = processBuilder.start()
                }
            }.catchOrRethrow { t ->
                if (t is IOException) {
                    Logger.tryLog { t.stackTraceToString() }
                    launchingErr = true
                    launchingErrMsg = "Could not start game process (IO Error)"
                    return@launch
                }
            }
            isLaunching = false
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

    fun userInputLaunchVanillaGame() {
        if (isLaunching) return
        val selectedPlatform = modManagerScreenModel.gameContext?.platform ?: return
        val gameInstallFolder = modManagerScreenModel.gameContext?.paths?.install ?: return
        val gameRootFolder = modManagerScreenModel.gameContext?.paths?.root ?: return
        val gameLauncherFile = modManagerScreenModel.gameContext?.paths?.launcher ?: return
        val gameBinaryFile = modManagerScreenModel.gameContext?.paths?.binary ?: return
        val gamePaksFolder = modManagerScreenModel.gameContext?.paths?.paks ?: return
        val gameVersion = modManagerScreenModel.gameContext?.version as? ManorLordsGameVersion ?: return
        isLaunching = true
        launchingErr = false
        launchingErrMsg = ""
        coroutineScope.launch {
            launchingStatusMsg = "Launching ..."
            withContext(Dispatchers.IO) {
                val gameFile = gameBinaryFile
                val gameFileDir = gameFile.parent ?: return@withContext

                runCatching {
                    val dwmApi = fs.file(gameFileDir, "dwmapi.dll")
                    if (dwmApi.isRegularFile()) {
                        runCatching {
                            dwmApi.delete()
                        }.catchOrRethrow { e ->
                            if (e is IOException) {
                                launchingErr = true
                                launchingErrMsg = "Could not delete ${dwmApi.path}"
                                return@withContext
                            }
                        }
                    }

                    val ue4ssDir = fs.file(gameFileDir, "ue4ss")
                    if (ue4ssDir.isDirectory()) {
                        runCatching {
                            ue4ssDir.deleteRecursively()
                        }.catchOrRethrow { e ->
                            if (e is IOException) {
                                launchingErr = true
                                launchingErrMsg = "Could not delete ${ue4ssDir.path} directory recursively"
                                return@withContext
                            }
                        }
                    }

                    val pakModsDir = fs.file(gamePaksFolder, "~mods")
                    if (pakModsDir.isDirectory()) {
                        runCatching {
                            pakModsDir.deleteRecursively()
                        }.catchOrRethrow { e ->
                            if (e is IOException) {
                                launchingErr = true
                                launchingErrMsg = "Could not delete ${pakModsDir.path} directory recursively"
                                return@withContext
                            }
                        }
                    }

                    val logicModsDir = fs.file(gamePaksFolder, "LogicMods")
                    if (logicModsDir.isDirectory()) {
                        runCatching {
                            logicModsDir.deleteRecursively()
                        }.catchOrRethrow { e ->
                            if (e is IOException) {
                                launchingErr = true
                                launchingErrMsg = "Could not delete ${logicModsDir.path} directory recursively"
                                return@withContext
                            }
                        }
                    }

                    val process = runCatching {
                        val exe = when (selectedPlatform) {
                            GamePlatform.Steam, GamePlatform.EpicGamesStore, GamePlatform.GogCom -> {
                                if (gameVersion == ManorLordsGameVersion.V_0_8_029a)
                                    gameBinaryFile
                                else
                                    gameLauncherFile
                            }
                            GamePlatform.XboxPcGamePass -> gameLauncherFile
                        }
                        val processBuilder = ProcessBuilder("cmd.exe", "/c", "start", "", fs.absolute(exe).toString())
                        processBuilder.start()
                    }.catchOrRethrow { e ->
                        if (e is IOException) {
                            launchingErr = true
                            launchingErrMsg = "Could not start game process (IO Error)"
                            return@withContext
                        }
                    }
                }.catchOrRethrow { e ->
                    Logger.tryLog { e.stackTraceToString() }
                    if (e is IOException) {
                        launchingErr = true
                        launchingErrMsg = "IO Error"
                        return@withContext
                    }
                }
            }
            isLaunching = false
        }
    }
}