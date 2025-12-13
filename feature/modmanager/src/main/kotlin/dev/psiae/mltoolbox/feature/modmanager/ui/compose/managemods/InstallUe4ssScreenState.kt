package dev.psiae.mltoolbox.feature.modmanager.ui.compose.managemods

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.fs.file.FileObject
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toFsPath
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toPath
import dev.psiae.mltoolbox.foundation.ui.compose.LocalAwtWindow
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
import net.lingala.zip4j.exception.ZipException
import java.awt.Window
import java.io.IOException
import java.io.InterruptedIOException
import kotlin.io.path.deleteRecursively

@Composable
fun rememberInstallUe4ssScreenState(
    goBack: () -> Unit,
    model: InstallUe4ssScreenModel,
    initializer: InstallUe4ssScreenState.() -> Unit = {}
): InstallUe4ssScreenState {
    val latestGoBack by rememberUpdatedState(goBack)
    val window = LocalAwtWindow.current
    return remember(model) {
        InstallUe4ssScreenState(
            _goBack = {
                latestGoBack.invoke()
            },
            window = window,
            model = model,
        ).apply(initializer)
    }
}

class InstallUe4ssScreenState(
    private val _goBack: () -> Unit,
    val window: Window,
    val model: InstallUe4ssScreenModel
) {

    val coroutineScope
        get() = model.coroutineScope

    val dispatch
        get() = model.context.dispatch

    val fs = FileSystem.SYSTEM

    var statusMessage by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isError by mutableStateOf(false)
        private set

    var isInstalledSuccessfully by mutableStateOf(false)
        private set

    var isLastSelectedArchiveInvalid by mutableStateOf(false)
        private set

    var isInvalidAppDirectory by mutableStateOf(false)
        private set

    var isInvalidGameDirectory by mutableStateOf(false)
        private set

    private var _pickUe4ssArchiveCompletion: Deferred<Path?>? = null

    var canExitScreen by mutableStateOf(false)
        private set

    val isBusy
        get() = _pickUe4ssArchiveCompletion?.isActive == true

    init {
        updateCanNavigateBack()
    }

    private fun updateCanNavigateBack() {
        canExitScreen = !isBusy
    }

    private fun updateState(
        block: (state: InstallUe4ssScreenState) -> Unit
    ) {
        val state = this
        block(state)
    }
    private fun updateStateBlocking(
        block: (state: InstallUe4ssScreenState) -> Unit
    ) {
        val state = this
        val task = coroutineScope.launch { block(state) }
        runBlocking { task.join() }
    }
    private fun updateStatePosting(
        block: (state: InstallUe4ssScreenState) -> Unit
    ) {
        val state = this
        val task = coroutineScope.launch { block(state) }
    }
    private suspend fun updateStateSuspending(
        block: (state: InstallUe4ssScreenState) -> Unit
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

    fun pickUe4ssArchive(
        path: Path?
    ) {

        if (_pickUe4ssArchiveCompletion?.isCompleted == false)
            return

        updateState { state ->
            state.isLoading = false
            state.isError = false
            state.isInstalledSuccessfully = false
            state.isLastSelectedArchiveInvalid = false
            state.isInvalidAppDirectory = false
            state.isInvalidGameDirectory = false
            state.statusMessage = ""
        }

        _pickUe4ssArchiveCompletion = coroutineScope.async(dispatch.mainDispatcher.dispatching) {
            val path = path ?: run {
                val pick = FileKit.pickFile(
                    type = PickerType.File(listOf("zip")),
                    mode = PickerMode.Single,
                    title = "Select downloaded UE4SS archive (*.zip)",
                    initialDirectory = null,
                    platformSettings = FileKitPlatformSettings(
                        parentWindow = window
                    )
                )
                if (pick == null)
                    return@async null
                pick.file.toFsPath()
            }
            if (!processPickedUe4ssArchive(path)) {
                return@async null
            }
            path
        }.apply {
            invokeOnCompletion { t ->
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

    fun dragAndDropUe4ssArchive(pathStr: String) {
        pickUe4ssArchive(java.nio.file.Path.of(java.net.URI(pathStr)).toFsPath())
    }

    private suspend fun processPickedUe4ssArchive(
        filePath: Path
    ): Boolean {

        updateState { state ->
            state.isLoading = true
            state.statusMessage = ""
        }

        val file = fs.file(filePath)
        val extractDirPath = MLToolboxApp.tempDir.toFsPath() / "ue4ss_install"

        var errorStatusMessage = ""
        val processedSuccessfully = withContext(dispatch.ioDispatcher) {
            runInterruptible { runCatching {
                if (fs.exists(extractDirPath)) {
                    if (!fs.file(extractDirPath).canDeleteRecursively()) {
                        errorStatusMessage = "Unable to lock 'ue4ss_install' dir, might be used somewhere else"
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

                runCatching {
                    ZipFile(file.path.toJFile()).use { zipFile ->
                        zipFile.extractAll(extractDirPath.toString())
                    }
                }.catchOrRethrow { e ->
                    when (e) {
                        is ZipException -> {
                            tryLog { "Unable to extract archive: ${e.stackTraceToString()}" }
                            errorStatusMessage = "Unable to extract archive"
                            return@runInterruptible false
                        }
                    }
                }

                updateStateBlocking { state ->
                    state.statusMessage = "verifying ..."
                }

                val dwmApiDllPath = extractDirPath / "dwmapi.dll"
                if (!fs.exists(dwmApiDllPath) || !fs.file(dwmApiDllPath).followLinks().isRegularFile()) {
                    errorStatusMessage = "archive missing 'dwmapi.dll'"
                    return@runInterruptible false
                }

                val ue4ssDllPath = extractDirPath / "ue4ss/UE4SS.dll"
                if (!fs.exists(ue4ssDllPath) || !fs.file(ue4ssDllPath).followLinks().isRegularFile()) {
                    errorStatusMessage = "archive missing 'ue4ss/UE4SS.dll'"
                    return@runInterruptible false
                }

                val ue4ssModsDir = extractDirPath / "ue4ss/Mods"
                if (!fs.exists(ue4ssModsDir) || !fs.file(ue4ssModsDir).followLinks().isDirectory()) {
                    errorStatusMessage = "archive missing 'ue4ss/Mods'"
                    return@runInterruptible false
                }

                updateStateBlocking { state ->
                    state.statusMessage = "verifying game dirs ..."
                }


                val gameDirPath = model.gameContext.paths.binary.parent
                checkNotNull(gameDirPath) { "gameDir is null" }
                check(!gameDirPath.isEmpty) { "gameDir is empty" }

                if (!fs.exists(gameDirPath) || !fs.file(gameDirPath).followLinks().isDirectory()) {
                    errorStatusMessage = "missing game directory"
                    return@runInterruptible false
                }

                val gameDwmApiDllPath = gameDirPath / "dwmapi.dll"
                if (fs.exists(gameDwmApiDllPath)) {
                    if (!fs.file(gameDwmApiDllPath).canDeleteRecursively()) {
                        errorStatusMessage = "unable to lock game 'dwmapi.dll' for deletion"
                        return@runInterruptible false
                    }
                }

                updateStateBlocking { state ->
                    state.statusMessage = "preparing extract dirs ..."
                }

                updateStateBlocking { state ->
                    state.statusMessage = "preparing game dirs ..."
                }

                val gameUe4ssFolderPath = gameDirPath / "ue4ss"
                if (fs.exists(gameUe4ssFolderPath)) {
                    if (!fs.file(gameUe4ssFolderPath).canDeleteRecursively()) {
                        errorStatusMessage = "unable to lock game 'ue4ss' folder for deletion"
                        return@runInterruptible false
                    }
                }

                if (fs.exists(gameDwmApiDllPath))
                    fs.file(gameDwmApiDllPath).deleteRecursively()

                if (fs.exists(gameUe4ssFolderPath))
                    fs.file(gameUe4ssFolderPath).deleteRecursively()

                updateStateBlocking { state ->
                    state.statusMessage = "installing ..."
                }

                fs.list(extractDirPath).forEach { p ->
                    fs.file(p).copyToRecursively(gameDirPath / p.name, followLinks = false, overwrite = true)
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
}