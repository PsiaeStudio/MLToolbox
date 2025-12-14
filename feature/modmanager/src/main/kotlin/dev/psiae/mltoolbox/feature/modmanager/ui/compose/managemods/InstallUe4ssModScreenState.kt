package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.github.junrar.Junrar
import com.github.junrar.exception.RarException
import com.github.junrar.exception.UnsupportedRarV5Exception
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.foundation.domain.ConfigurationGuard
import dev.psiae.mltoolbox.foundation.domain.ConfigurationKey
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.fs.file.JFile
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toFsPath
import dev.psiae.mltoolbox.foundation.ui.compose.LocalAwtWindow
import dev.psiae.mltoolbox.foundation.ui.compose.ScreenState
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import java.awt.Window
import java.io.IOException
import java.io.InterruptedIOException
import java.io.RandomAccessFile
import kotlin.stackTraceToString

@Composable
fun rememberInstallUe4ssModScreenState(
    goBack: () -> Unit,
    model: InstallUe4ssModScreenModel,
    initializer: InstallUe4ssModScreenState.() -> Unit = {}
): InstallUe4ssModScreenState {
    val latestGoBack by rememberUpdatedState(goBack)
    val window = LocalAwtWindow.current
    return remember(model) {
        InstallUe4ssModScreenState(
            _goBack = {
                latestGoBack.invoke()
            },
            window = window,
            model = model,
        ).apply(initializer)
    }
}

class InstallUe4ssModScreenState(
    private val _goBack: () -> Unit,
    val window: Window,
    val model: InstallUe4ssModScreenModel
) : ScreenState(model.context) {

    val fs = FileSystem.SYSTEM
    
    val dispatch
        get() = context.dispatch

    var statusMessage by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isError by mutableStateOf(false)
        private set

    var isInstalledSuccessfully by mutableStateOf(false)
        private set

    private var _pickUe4ssModArchiveCompletion: Deferred<List<Path>?>? = null

    var canExitScreen by mutableStateOf(false)
        private set

    val isBusy
        get() = _pickUe4ssModArchiveCompletion?.isActive == true

    init {
        updateCanNavigateBack()
    }

    private fun updateCanNavigateBack() {
        canExitScreen = !isBusy
    }

    private fun updateState(
        block: (state: InstallUe4ssModScreenState) -> Unit
    ) {
        val state = this
        block(state)
    }
    private fun updateStateBlocking(
        block: (state: InstallUe4ssModScreenState) -> Unit
    ) {
        val state = this
        val task = coroutineScope.launch { block(state) }
        runBlocking { task.join() }
    }
    private fun updateStatePosting(
        block: (state: InstallUe4ssModScreenState) -> Unit
    ) {
        val state = this
        val task = coroutineScope.launch { block(state) }
    }
    private suspend fun updateStateSuspending(
        block: (state: InstallUe4ssModScreenState) -> Unit
    ) {
        val state = this
        withContext(dispatch.mainDispatcher) {
            block(state)
        }
    }

    private fun tryLog(getMsg: () -> String) = Logger.tryLog(getMsg)

    fun exitScreen() {
        if (isBusy)
            return
        _goBack()
    }

    fun pickUe4ssModArchives(
        paths: List<Path>?
    ) {
        if (_pickUe4ssModArchiveCompletion?.isCompleted == false)
            return

        updateState { state ->
            state.isLoading = false
            state.isError = false
            state.isInstalledSuccessfully = false
            state.statusMessage = ""
        }

        var blockingConfig: ConfigurationKey? = null
        var blockingMsg: String? = null
        val configKeys = setOf(ModManagerGameContext)
        val configGuard = model.configWarden.placeGuardIf(
            configKeys,
            { key, guards ->
                guards.firstOrNull { it.isEditing }?.also {
                    blockingConfig = key
                    blockingMsg = it.reason
                } == null
            },
            {
                ConfigurationGuard(
                    "InstallUe4ssModScreen_install",
                    "Installing UE4SS mod",
                    false
                )
            }
        )
        if (configGuard == null) {
            updateState { state ->
                state.isLoading = false
                state.isError = true
                state.statusMessage = "Config '${blockingConfig?.displayString}' currently in use: $blockingMsg"
            }
            return
        }


        _pickUe4ssModArchiveCompletion = coroutineScope.async(dispatch.mainDispatcher.dispatching) {
            val paths = paths ?: run {
                val pick = FileKit.pickFile(
                    type = PickerType.File(listOf("zip", "rar", "7z")),
                    mode = PickerMode.Multiple(),
                    title = "Select downloaded UE4SS Mod archives (*.zip, *.rar, *.7z)",
                    initialDirectory = "",
                    platformSettings = FileKitPlatformSettings(
                        parentWindow = window
                    )
                )
                if (pick == null)
                    return@async null
                pick.map { it.file.toFsPath() }
            }
            if (!processPickedUe4ssArchive(paths)) {
                return@async null
            }
            paths
        }.apply {
            invokeOnCompletion { t ->
                model.configWarden.removeGuard(configGuard)
                if (t == null) {
                    updateStatePosting { state -> state.updateCanNavigateBack() }
                }
                if (t == null || t is Exception) {
                    Runtime.getRuntime().gc()
                }
            }
        }
        coroutineScope.launch {
            _pickUe4ssModArchiveCompletion?.await()
        }
        updateState { state ->
            state.updateCanNavigateBack()
        }
    }

    fun dragAndDropUe4ssModArchives(pathStrings: List<String>) {
        pickUe4ssModArchives(pathStrings.map { java.nio.file.Path.of(java.net.URI(it)).toFsPath() })
    }

    private suspend fun processPickedUe4ssArchive(
        filePaths: List<Path>
    ): Boolean {

        updateState { state ->
            state.isLoading = true
            state.statusMessage = ""
        }

        val extractDirPath = MLToolboxApp.tempDir.toFsPath() / "ue4ss_mods_install"

        var errorStatusMessage = ""
        val processedSuccessfully = withContext(dispatch.ioDispatcher) {
            runInterruptible { runCatching {

                updateStateBlocking { state ->
                    state.statusMessage = "preparing extract dirs ..."
                }

                if (fs.exists(extractDirPath)) {
                    if (!fs.file(extractDirPath).canDeleteRecursively()) {
                        errorStatusMessage = "Unable to lock 'ue4ss_mods_install' dir, might be used somewhere else"
                        return@runInterruptible false
                    }
                    fs.file(extractDirPath).deleteRecursively()
                    fs.createDirectories(extractDirPath)
                } else {
                    fs.createDirectories(extractDirPath)
                }

                updateStateBlocking { state ->
                    state.statusMessage = "extracting ..."
                }

                filePaths.forEach { path ->
                    val dest = extractDirPath / path.name
                    fs.createDirectories(dest)
                    extractModArchive(path, dest)
                }

                updateStateBlocking { state ->
                    state.statusMessage = "verifying mods ..."
                }

                val listExtractDirectory = fs.list(extractDirPath).filter {
                    fs.file(it).isDirectory()
                }
                if (listExtractDirectory.isEmpty()) {
                    errorStatusMessage = "ue4ss_mods_install folder is empty"
                    return@runInterruptible false
                }

                val modsToBeInstalledNames = mutableSetOf<String>()
                val modsToBeInstalledDir = mutableListOf<Path>()
                var needLogicModsFolder = false
                listExtractDirectory.forEach { folderPath ->
                    val listFiles = fs.list(folderPath)
                    if (listFiles.isEmpty() || listFiles.size > 1 || !fs.file(listFiles.first()).isDirectory()) {
                        errorStatusMessage = "'${folderPath.name}' must only contain one root directory, given archive is not a UE4SS mod"
                        return@runInterruptible false
                    }

                    val rootDir = listFiles.first()
                    if (!modsToBeInstalledNames.add(rootDir.name)) {
                        errorStatusMessage = "Duplicate mod name: ${rootDir.name}"
                        return@runInterruptible false
                    }
                    modsToBeInstalledDir.add(rootDir)

                    val ae_bp = rootDir / "ae_bp"
                    if (fs.file(ae_bp).isDirectory()) {
                        val modBp = ae_bp / "${rootDir.name}.pak"
                        if (fs.exists(modBp))
                            needLogicModsFolder = true
                    }

                    val dllsMain = rootDir / "dlls" / "main.dll"
                    if (fs.exists(dllsMain))
                        return@forEach

                    val scriptsMain =  rootDir / "scripts" / "main.lua"
                    if (fs.exists(scriptsMain))
                        return@forEach

                    errorStatusMessage = "${rootDir} is missing entry point (dlls\\main.dll or Scripts\\main.lua), given archive is not a UE4SS mod"
                    return@runInterruptible false
                }


                updateStateBlocking {
                    statusMessage = "verifying target game dir ..."
                }

                val gameDir = model.gameContext.paths.binary.parent
                if (gameDir == null || !fs.file(gameDir).followLinks().isDirectory()) {
                    errorStatusMessage = "missing game directory"
                    return@runInterruptible false
                }

                val ue4ssDir = gameDir / "ue4ss"
                if (!fs.file(ue4ssDir).followLinks().isDirectory()) {
                    errorStatusMessage = "missing ue4ss directory, make sure 'RE-UE4SS' Mod Loader is already installed"
                    return@runInterruptible false
                }

                updateStateBlocking {
                    statusMessage = "verifying target game Mods dir ..."
                }

                val modsDir = ue4ssDir / "mods"
                if (!fs.file(modsDir).followLinks().isDirectory()) {
                    errorStatusMessage = "missing ue4ss/Mods directory"
                    return@runInterruptible false
                }


                val directoriesToOverwrite = fs.list(modsDir).filter {
                    it.name in modsToBeInstalledNames
                }

                directoriesToOverwrite.forEach {
                    if (!fs.file(it).canDeleteRecursively()) {
                        errorStatusMessage = "Failed to lock existing mod directory for deletion: ${it.name}"
                        return@runInterruptible false
                    }
                }

                if (needLogicModsFolder) {
                    updateStateBlocking { state ->
                        state.statusMessage = "verifying LogicMods dir ..."
                    }
                    val paksDirPath = model.gameContext.paths.paks
                    if (!fs.file(paksDirPath).followLinks().isDirectory()) {
                        errorStatusMessage = "'Paks' folder does not exist"
                        return@runInterruptible false
                    }
                    val logicsModDirPath = paksDirPath / "LogicMods"
                    if (fs.file(logicsModDirPath).followLinks().isDirectory()) {
                        modsToBeInstalledNames.forEach { e ->
                            val targetFilePath = logicsModDirPath / "$e.pak"
                            if (fs.exists(targetFilePath))
                                if (!fs.file(targetFilePath).canDeleteRecursively()) {
                                    errorStatusMessage = "Unable to lock LogicMods file for deletion: $e.pak"
                                    return@runInterruptible false
                                }

                            /*val targetFolderPath = logicsModDirPath / e
                            if (fs.exists(targetFolderPath))
                                if (!fs.file(targetFolderPath).canDeleteRecursively()) {
                                    errorStatusMessage = "Unable to lock LogicMods folder for deletion: $e"
                                    return@runInterruptible false
                                }*/
                        }
                    }

                }

                updateStateBlocking { state ->
                    state.statusMessage = "preparing game dirs ..."
                }


                directoriesToOverwrite.forEach {
                    fs.file(it).deleteRecursively()
                }

                if (needLogicModsFolder) {
                    updateStateBlocking { state ->
                        state.statusMessage = "preparing LogicMods dirs ..."
                    }
                    val paksDirPath = model.gameContext.paths.paks
                    val logicsModDirPath = paksDirPath / "LogicMods"
                    if (fs.file(logicsModDirPath).exists()) {
                        if (!fs.file(logicsModDirPath).followLinks().isDirectory()) {
                            fs.file(logicsModDirPath).deleteRecursively()
                            fs.createDirectory(logicsModDirPath)
                        }
                    } else {
                        fs.createDirectory(logicsModDirPath)
                    }

                    modsToBeInstalledNames.forEach { e ->
                        val targetFilePath = logicsModDirPath / "$e.pak"
                        if (fs.exists(targetFilePath))
                            fs.file(targetFilePath).deleteRecursively()

                        /*val targetFolderPath = logicsModDirPath / e
                        if (fs.exists(targetFolderPath))
                            fs.file(targetFolderPath).deleteRecursively()*/
                    }
                }

                updateStateBlocking { state ->
                    state.statusMessage = "installing ..."
                }

                modsToBeInstalledDir.forEach { e ->
                    fs.file(e).copyToRecursively(
                        targetPath = modsDir / e.name
                    )
                }

                modsToBeInstalledDir.forEach { e ->
                    val ae_bp = e / "ae_bp"
                    if (fs.file(ae_bp).isDirectory()) {
                        val modBP  = ae_bp / "${e.name}.pak"
                        if (fs.exists(modBP)) {
                            val paksDirPath = model.gameContext.paths.paks
                            val logicsModDirPath = paksDirPath / "LogicMods"
                            fs.file(modBP).copyTo(logicsModDirPath / modBP.name)
                        }
                    }
                }

                true
            }.catchOrRethrow { e ->
                tryLog {
                    e.stackTraceToString()
                }
                when (e) {
                    is InterruptedIOException -> {
                        errorStatusMessage = "Unable to install, IO interrupted, see logs for details"
                        return@runInterruptible false
                    }
                    is IOException -> {
                        errorStatusMessage = "Unable to install, IO error, see logs for details"
                        return@runInterruptible false
                    }
                }
            }.getOrThrow() }
        }




        updateState { state ->
            state.isLoading = false
            if (processedSuccessfully) {
                state.isInstalledSuccessfully = true
                state.statusMessage = ""
            }
            else {
                state.isError = true
                state.statusMessage = errorStatusMessage
            }
        }

        return processedSuccessfully
    }

    private fun extractModArchive(
        path: Path,
        dest: Path
    ) {
        val pathStr = path.toString()
        if (pathStr.endsWith(".rar", ignoreCase = true)) {
            extractRarArchive(path, dest)
        } else if (pathStr.endsWith(".7z", ignoreCase = true)) {
            extract7zArchive(path, dest)
        } else if (pathStr.endsWith(".zip", ignoreCase = true)) {
            extractZipArchive(path, dest)
        } else error("unexpected file extension: ${pathStr.substringAfterLast('.', "")}")
    }

    private fun extractRarArchive(
        path: Path,
        dest: Path
    ) {
        val jFile = path.toJFile()
        runCatching {
            Junrar.extract(jFile, dest.toJFile())
        }.catchOrRethrow { e ->
            if (e is UnsupportedRarV5Exception) {
                return extractRar5Archive(path, dest)
            }
            if (e is RarException)
                throw IOException("RarException: ${e.message}", e)
        }
    }

    //
    // TODO: replace with our fs and compress module
    //

    private fun extractRar5Archive(
        path: Path,
        dest: Path
    ) {

        val jFile = path.toJFile()
        val destJFile = dest.toJFile()
        RandomAccessFile(jFile, "r").use { r ->
            RandomAccessFileInStream(r).use { sevenZipR ->
                SevenZip
                    .openInArchive(ArchiveFormat.RAR5, sevenZipR)
                    .use { inArchive ->
                        val destCanonical = destJFile.canonicalFile
                        inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                            val simple = inArchive.simpleInterface
                            for (item in simple.archiveItems) {
                                val entryPath = item.path ?: continue
                                val outFile = JFile(destCanonical, entryPath).canonicalFile

                                if (item.isFolder) {
                                    outFile.mkdirs()
                                    continue
                                }

                                outFile.parentFile?.mkdirs()

                                RandomAccessFile(outFile, "rw").use { outRaf ->
                                    outRaf.setLength(0)
                                    val result = item.extractSlow(RandomAccessFileOutStream(outRaf))
                                    if (result != ExtractOperationResult.OK) {
                                        throw IOException("Extraction failed for ${item.path}: $result")
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun extract7zArchive(
        path: Path,
        dest: Path
    ) {
        val file = path.toJFile()
        val destJFile = dest.toJFile()
        RandomAccessFile(file, "r").use { r ->
            RandomAccessFileInStream(r).use { sevenZipR ->
                SevenZip
                    .openInArchive(ArchiveFormat.SEVEN_ZIP, sevenZipR)
                    .use { inArchive ->
                        val destCanonical = destJFile.canonicalFile
                        inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                            val simple = inArchive.simpleInterface
                            for (item in simple.archiveItems) {
                                val entryPath = item.path ?: continue
                                val outFile = JFile(destCanonical, entryPath).canonicalFile

                                if (item.isFolder) {
                                    outFile.mkdirs()
                                    continue
                                }

                                outFile.parentFile?.mkdirs()

                                RandomAccessFile(outFile, "rw").use { outRaf ->
                                    outRaf.setLength(0)
                                    val result = item.extractSlow(RandomAccessFileOutStream(outRaf))
                                    if (result != ExtractOperationResult.OK) {
                                        throw IOException("Extraction failed for ${item.path}: $result")
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun extractZipArchive(
        path: Path,
        dest: Path
    ) {
        ZipFile(path.toJFile()).use { zipFile ->
            zipFile.extractAll(dest.toJFile().absolutePath)
        }
    }
}