package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.runtime.*
import com.sun.nio.file.ExtendedOpenOption
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.shared.java.jFile
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.*
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.awt.Window
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.*
import java.util.Comparator
import kotlin.io.AccessDeniedException
import kotlin.io.FileAlreadyExistsException
import kotlin.io.NoSuchFileException
import kotlin.io.path.*

@Composable
fun rememberDirectInstallUE4SSScreenState(
    directInstallModScreenState: DirectInstallModScreenState
): DirectInstallUE4SSScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(directInstallModScreenState) {
        DirectInstallUE4SSScreenState(directInstallModScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }


    return state
}

class DirectInstallUE4SSScreenState(
    val directInstallModScreenState: DirectInstallModScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    private var pickUE4SSArchiveCompletion: Deferred<jFile?>? = null

    var selectedUE4SSArchive by mutableStateOf<jFile?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isInstalledSuccessfully by mutableStateOf(false)
        private set

    var isLastSelectedArchiveInvalid by mutableStateOf(false)
        private set

    var isInvalidGameDirectory by mutableStateOf(false)
        private set

    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {

    }

    fun pickUE4SSArchive(awtWindow: Window) {
        coroutineScope.launch {
            if (pickUE4SSArchiveCompletion?.isActive != true) {
                isLastSelectedArchiveInvalid = false
                isInvalidGameDirectory = false
                isInstalledSuccessfully = false
                statusMessage = null
                async {
                    val pick = FileKit.pickFile(
                        type = PickerType.File(listOf("zip")),
                        mode = PickerMode.Single,
                        title = "Select downloaded UE4SS archive (*.zip)",
                        initialDirectory = null,
                        platformSettings = FileKitPlatformSettings(
                            parentWindow = awtWindow
                        )
                    )
                    if (pick == null) {
                        return@async null
                    }
                    if (!processPickedFile(pick.file)) {
                        return@async null
                    }
                    pick.file
                }.also {
                    pickUE4SSArchiveCompletion = it
                    runCatching {
                        it.await()
                    }.fold(
                        onSuccess = {
                            Runtime.getRuntime().gc()
                        },
                        onFailure = { t ->
                            if (t is Exception) {
                                Runtime.getRuntime().gc()
                            }
                            throw t
                        }
                    )
                }
            }
        }
    }

    fun userDropUE4SSArchive(file: jFile) {
        coroutineScope.launch {
            if (pickUE4SSArchiveCompletion?.isActive != true) {
                isLastSelectedArchiveInvalid = false
                isInvalidGameDirectory = false
                isInstalledSuccessfully = false
                statusMessage = null
                async {
                    processPickedFile(file)
                    file
                }.also {
                    pickUE4SSArchiveCompletion = it
                    runCatching {
                        it.await()
                    }.fold(
                        onSuccess = {
                            Runtime.getRuntime().gc()
                        },
                        onFailure = { t ->
                            if (t is Exception) {
                                Runtime.getRuntime().gc()
                            }
                            throw t
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    suspend fun processPickedFile(file: jFile): Boolean {
        isLoading = true
        isLastSelectedArchiveInvalid = false
        isInvalidGameDirectory = false
        isInstalledSuccessfully = false
        statusMessage = "..."
        val gameBinaryFile = directInstallModScreenState.manageDirectModsScreenState.manageModsScreenState.modManagerScreenState.requireGameBinaryFile()
        withContext(Dispatchers.IO) {
            statusMessage = "preparing ..."

            val userDir = jFile(System.getProperty("user.dir"))
            val installDir = jFile(userDir.absolutePath + "\\MLToolboxApp\\temp\\ue4ss_install")
            run {
                var lockedFile: jFile? = null
                if (installDir.exists() && !run {
                    var open = true
                    open = installDir.toPath().walk(PathWalkOption.INCLUDE_DIRECTORIES).all { f ->
                        if (f.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                            var ch: FileChannel? = null
                            try {
                                ch = FileChannel.open(
                                    f,
                                    if (f.isWritable()) StandardOpenOption.WRITE else StandardOpenOption.READ,
                                    StandardOpenOption.READ,
                                    ExtendedOpenOption.NOSHARE_READ,
                                    ExtendedOpenOption.NOSHARE_WRITE,
                                    ExtendedOpenOption.NOSHARE_DELETE
                                )
                            } catch (ex: IOException) {
                                open = false
                                lockedFile = f.toFile()
                            } finally {
                                ch?.close()
                            }
                        }
                        open
                    }
                    open
                }) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to lock ue4ss_install directory from app directory, ${lockedFile?.let {
                        it.absolutePath
                            .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                            .replace(' ', '\u00A0')
                    }} might be opened in another process"
                    return@withContext
                }
            }

            if (installDir.exists())
                installDir.toPath()
                    .walk(PathWalkOption.INCLUDE_DIRECTORIES)
                    .sortedWith(Comparator.reverseOrder())
                    .forEach { f ->
                        runCatching {
                            if (!f.isWritable()) {
                                val setWriteable = f.toFile().setWritable(true)
                                if (!setWriteable)
                                    throw IOException("Unable to make file Writable for deletion")
                            }
                            f.deleteExisting()
                        }
                        .onFailure { e ->
                            when (e) {
                                is NoSuchFileException, is DirectoryNotEmptyException, is IOException -> {
                                    isLoading = false
                                    isInvalidGameDirectory = true
                                    statusMessage = "unable to delete ${f.toFile().let {
                                        it.absolutePath
                                            .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                                            .replace(' ', '\u00A0')
                                    }} from app directory, it might be opened in another process"
                                    return@withContext
                                }
                            }
                        }
                    }

            statusMessage = "extracting ..."
            runCatching {
                ZipFile(file).use { zipFile ->
                    zipFile.extractAll(System.getProperty("user.dir") + "\\MLToolboxApp\\temp\\ue4ss_install")
                }
            }.onFailure { ex ->
                if (ex is ZipException) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "unable to extract archive"
                    return@withContext
                }
                throw ex
            }

            statusMessage = "verifying ..."
            val dir = System.getProperty("user.dir") + "\\MLToolboxApp\\temp\\ue4ss_install"

            val dwmApiDll = jFile("$dir\\dwmapi.dll").exists()
            if (!dwmApiDll) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "missing 'dwmapi.dll', given archive is not the Mod Loader"
                return@withContext
            }
            val ue4ssDll = jFile("$dir\\ue4ss\\UE4SS.dll").exists()
            if (!ue4ssDll) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "missing 'ue4ss\\UE4SS.dll', given archive is not the Mod Loader"
                return@withContext
            }
            statusMessage = "preparing install ..."

            val gameDir = gameBinaryFile?.parentFile
            if (gameDir == null || !gameDir.exists()) {
                isLoading = false
                isInvalidGameDirectory = true
                statusMessage = "missing game directory"
                return@withContext
            }
            runCatching {

                val gameDwmApi = jFile("$gameDir\\dwmapi.dll")
                val gameDwmApiPath = gameDwmApi.toPath()
                if (gameDwmApiPath.exists() && !run {
                    var open = true
                    var ch: FileChannel? = null
                    try {
                        ch = FileChannel.open(
                            gameDwmApiPath,
                            if (gameDwmApiPath.isWritable()) StandardOpenOption.WRITE else StandardOpenOption.READ,
                            StandardOpenOption.READ,
                            ExtendedOpenOption.NOSHARE_READ,
                            ExtendedOpenOption.NOSHARE_WRITE,
                            ExtendedOpenOption.NOSHARE_DELETE
                        )
                    } catch (ex: IOException) {
                        open = false
                    } finally {
                        ch?.close()
                    }
                    open
                }) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to lock dwmapi.dll from game directory, it might be opened in another process"
                    return@withContext
                }
                val ue4ssFolder = jFile("$gameDir\\ue4ss")
                var lockedFile: jFile? = null
                if (ue4ssFolder.exists() && !run {
                    var open = true
                    open = ue4ssFolder.toPath().walk(PathWalkOption.INCLUDE_DIRECTORIES).all { f ->
                        if (f.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                            var ch: FileChannel? = null
                            try {
                                ch = FileChannel.open(
                                    f,
                                    if (f.isWritable()) StandardOpenOption.WRITE else StandardOpenOption.READ,
                                    StandardOpenOption.READ,
                                    ExtendedOpenOption.NOSHARE_READ,
                                    ExtendedOpenOption.NOSHARE_WRITE,
                                    ExtendedOpenOption.NOSHARE_DELETE
                                )
                            } catch (ex: IOException) {
                                open = false
                                lockedFile = f.toFile()
                            } finally {
                                ch?.close()
                            }
                        }
                        open
                    }
                    open
                }) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to lock ue4ss folder from game directory, ${lockedFile?.let {
                        it.absolutePath
                            .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                            .replace(' ', '\u00A0')
                    }} might be opened in another process"
                    return@withContext
                }
                if (gameDwmApi.exists() && !gameDwmApi.delete()) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to delete dwmapi.dll from game directory, it might be opened in another process"
                    return@withContext
                }

                if (ue4ssFolder.exists())
                    ue4ssFolder.toPath()
                        .walk(PathWalkOption.INCLUDE_DIRECTORIES)
                        .sortedWith(Comparator.reverseOrder())
                        .forEach { f ->
                            runCatching {
                                if (!f.isWritable()) {
                                    val setWriteable = f.toFile().setWritable(true)
                                    if (!setWriteable)
                                        throw IOException("Unable to make file Writable for deletion")
                                }
                                f.deleteExisting()
                            }
                            .onFailure { e ->
                                when (e) {
                                    is NoSuchFileException, is DirectoryNotEmptyException, is IOException -> {
                                        isLoading = false
                                        isInvalidGameDirectory = true
                                        statusMessage = "unable to delete ${f.toFile().let {
                                            it.absolutePath
                                                .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                                                .replace(' ', '\u00A0')
                                        }} from game directory, it might be opened in another process"
                                        return@withContext
                                    }
                                }
                            }
                        }
            }.onFailure { ex ->
                throw ex
            }
            statusMessage = "installing ..."
            runCatching {
                jFile(dir).copyRecursively(gameDir, true)
            }.onFailure { ex ->
                isLoading = false
                isInvalidGameDirectory = true
                statusMessage = when (ex) {
                    is FileNotFoundException -> {
                        "unable to copy recursively, source file is missing"
                    }
                    is FileAlreadyExistsException -> {
                        "unable to copy recursively, target file is not writeable"
                    }
                    is AccessDeniedException -> {
                        "unable to copy recursively, access denied"
                    }
                    is IOException -> {
                        "unable to copy recursively, IO error"
                    }
                    else -> throw ex
                }
                return@withContext
            }
            isLoading = false
            isInstalledSuccessfully = true
        }
        return false
    }
}