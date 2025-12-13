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
import dev.psiae.mltoolbox.core.utils.anyDuplicate
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.foundation.domain.ConfigurationGuard
import dev.psiae.mltoolbox.foundation.domain.ConfigurationKey
import dev.psiae.mltoolbox.foundation.fs.FileSystem
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
import kotlin.collections.forEach
import kotlin.collections.map

import dev.psiae.mltoolbox.foundation.fs.file.JFile
import java.nio.file.Files

@Composable
fun rememberInstallUEPakModScreenState(
    goBack: () -> Unit,
    model: InstallUEPakModScreenModel,
    initializer: InstallUEPakModScreenState.() -> Unit = {},
): InstallUEPakModScreenState {
    val latestGoBack by rememberUpdatedState(goBack)
    val window = LocalAwtWindow.current
    return remember(model) {
        InstallUEPakModScreenState(
            _goBack = {
                latestGoBack.invoke()
            },
            window = window,
            model = model,
        ).apply(initializer)
    }
}

class InstallUEPakModScreenState(
    val _goBack: () -> Unit,
    val window: Window,
    val model: InstallUEPakModScreenModel
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
        block: (state: InstallUEPakModScreenState) -> Unit
    ) {
        val state = this
        block(state)
    }
    private fun updateStateBlocking(
        block: (state: InstallUEPakModScreenState) -> Unit
    ) {
        val state = this
        val task = coroutineScope.launch { block(state) }
        runBlocking { task.join() }
    }
    private fun updateStatePosting(
        block: (state: InstallUEPakModScreenState) -> Unit
    ) {
        val state = this
        val task = coroutineScope.launch { block(state) }
    }
    private suspend fun updateStateSuspending(
        block: (state: InstallUEPakModScreenState) -> Unit
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

    fun pickUEPakModArchives(
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
                    "InstallUEPakModScreen_install",
                    "Installing UE Pak mod",
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
                    title = "Select downloaded UE Pak Mod archives (*.zip, *.rar, *.7z)",
                    initialDirectory = "",
                    platformSettings = FileKitPlatformSettings(
                        parentWindow = window
                    )
                )
                if (pick == null)
                    return@async null
                pick.map { it.file.toFsPath() }
            }
            if (!processPickedUEPakModArchives(paths)) {
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
        updateState { state ->
            state.updateCanNavigateBack()
        }
    }

    fun dragAndDropUEPakModArchives(pathStrings: List<String>) {
        pickUEPakModArchives(pathStrings.map { java.nio.file.Path.of(java.net.URI(it)).toFsPath() })
    }

    private suspend fun processPickedUEPakModArchives(
        filePaths: List<Path>
    ): Boolean {

        updateState { state ->
            state.isLoading = true
            state.statusMessage = ""
        }

        val extractDirPath = MLToolboxApp.tempDir.toFsPath() / "ue_pak_mods_install"

        var errorStatusMessage = ""
        val processedSuccessfully = withContext(dispatch.ioDispatcher) {
            runInterruptible { runCatching {

                if (fs.exists(extractDirPath)) {
                    if (!fs.file(extractDirPath).canDeleteRecursively()) {
                        errorStatusMessage = "Unable to lock 'ue_pak_mods_install' dir, might be used somewhere else"
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
                    fs.file().isDirectory()
                }
                if (listExtractDirectory.isEmpty()) {
                    errorStatusMessage = "ue_pak_mods_install folder is empty"
                    return@runInterruptible false
                }

                val modsToBeInstalledNames = mutableSetOf<String>()
                val modsToBeInstalledFile = mutableSetOf<Path>()
                listExtractDirectory.forEach { folderPath ->
                    val listFiles = fs.list(folderPath)
                    if (listFiles.isEmpty() ||
                        run {
                            val singleDir = listFiles.filter { fs.file(it).isDirectory() }.size == 1
                            val singleFile = listFiles.filter { fs.file(it).isRegularFile() }.size == 1
                            !(singleFile xor singleDir)
                        }

                    ) {
                        errorStatusMessage = "'${folderPath.name}' must only contain one root file or one root directory, given archive is not a Pak mod"
                        return@runInterruptible false
                    }

                    val file = run {
                        val root = listFiles.first()
                        if (fs.file(root).isRegularFile())
                            return@run root
                        val rootFiles = fs.list(root)
                        if (rootFiles.isEmpty() || rootFiles.size > 1) {
                            errorStatusMessage = "'${root.name}' must only contain one root file or one root directory with single file, given archive is not a Pak mod"
                            return@runInterruptible false
                        }
                        rootFiles.first()
                    }

                    if (!file.toString().endsWith(".pak")) {
                        errorStatusMessage = "'${file.name}' is missing entry point '*.pak', given archive is not a Pak mod"
                        return@runInterruptible false
                    }
                    if (!modsToBeInstalledNames.add(file.name)) {
                        errorStatusMessage = "Duplicate mod name: ${file.name}"
                        return@runInterruptible false
                    }
                    modsToBeInstalledFile.add(file)
                }


                updateStateBlocking {
                    statusMessage = "verifying '~mods' folder ..."
                }


                val paksDirPath = model.gameContext.paths.paks
                if (!fs.file(paksDirPath).followLinks().isDirectory()) {
                    errorStatusMessage = "missing 'Paks' directory"
                    return@runInterruptible false
                }

                val modsDirPath = paksDirPath / "~mods"

                modsToBeInstalledNames.forEach { name ->
                    val existingPath = modsDirPath / name
                    if (fs.exists(existingPath)) {
                        if (!fs.file(existingPath).canDeleteRecursively()) {
                            errorStatusMessage = "Unable to lock file in '~mods' for deletion: $name"
                            return@runInterruptible false
                        }
                    }
                }

                updateStateBlocking {
                    statusMessage = "preparing '~mods' folder ..."
                }

                if (fs.file(modsDirPath).exists()) {
                    if (!fs.file(modsDirPath).followLinks().isDirectory()) {
                        if (!fs.file(modsDirPath).canDeleteRecursively()) {
                            errorStatusMessage = "Unable to lock file for deletion: '~mods'"
                            return@runInterruptible false
                        }
                        fs.file(modsDirPath).deleteRecursively()
                    }
                    fs.createDirectoryIfNotExist(modsDirPath)
                } else {
                    fs.createDirectoryIfNotExist(modsDirPath)
                }

                modsToBeInstalledNames.forEach { e ->
                    val targetFilePath = modsDirPath / e
                    if (fs.exists(targetFilePath))
                        fs.file(targetFilePath).deleteRecursively()
                }

                updateStateBlocking {
                    statusMessage = "installing ..."
                }

                modsToBeInstalledFile.forEach { e ->
                    fs.file(e).copyTo(modsDirPath / e.name)
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