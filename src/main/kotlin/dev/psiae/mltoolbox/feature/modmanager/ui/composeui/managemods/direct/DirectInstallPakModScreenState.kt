package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.runtime.*
import com.github.junrar.Junrar
import com.github.junrar.exception.RarException
import com.github.junrar.exception.UnsupportedRarV5Exception
import com.sun.nio.file.ExtendedOpenOption
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.utils.isNullOrNotActive
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.*
import net.lingala.zip4j.ZipFile
import net.sf.sevenzipjbinding.*
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import java.awt.Window
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption
import java.util.Comparator
import kotlin.io.path.*

@Composable
fun rememberDirectInstallUEPakModScreenState(
    directInstallModScreenState: DirectInstallModScreenState
): DirectInstallUEPakModScreenState {
    val composeUIContext = LocalComposeUIContext.current
    val state = remember(directInstallModScreenState) {
        DirectInstallUEPakModScreenState(directInstallModScreenState, composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class DirectInstallUEPakModScreenState(
    val directInstallModScreenState: DirectInstallModScreenState,
    val uiContext: ComposeUIContext
) {
    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    private var pickUEPakModsArchiveCompletion: Deferred<List<jFile>?>? = null

    var selectedUE4SSModsArchive by mutableStateOf<List<jFile>?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isInstalledSuccessfully by mutableStateOf(false)
        private set

    var isLastSelectedArchiveInvalid by mutableStateOf(false)
        private set

    var isInvalidModsDirectory by mutableStateOf(false)
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

    fun pickUE4PakModsArchive(awtWindow: Window) {
        coroutineScope.launch {
            if (pickUEPakModsArchiveCompletion?.isActive != true) {
                isLastSelectedArchiveInvalid = false
                isInvalidModsDirectory = false
                isInstalledSuccessfully = false
                statusMessage = null
                async {
                    val pick = FileKit.pickFile(
                        type = PickerType.File(listOf("zip", "rar", "7z")),
                        mode = PickerMode.Multiple(),
                        title = "Select downloaded UE Pak Mod(s) archive (*.zip, *.rar, *.7z)",
                        initialDirectory = "C:",
                        platformSettings = FileKitPlatformSettings(
                            parentWindow = awtWindow
                        )
                    )
                    if (pick == null) {
                        return@async null
                    }
                    if (!processSelectedModsArchive(pick.map { it.file })) {
                        return@async null
                    }
                    pick.map { it.file }
                }.also {
                    pickUEPakModsArchiveCompletion = it
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

    fun userDropUEPakModsArchive(
        mods: List<jFile>
    ) {
        if (mods.isEmpty()) return
        coroutineScope.launch {
            if (pickUEPakModsArchiveCompletion.isNullOrNotActive()) {
                async {
                    processSelectedModsArchive(mods)
                    mods
                }.also {
                    pickUEPakModsArchiveCompletion = it
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
    private suspend fun processSelectedModsArchive(mods: List<jFile>): Boolean {
        isLoading = true
        isLastSelectedArchiveInvalid = false
        isInvalidModsDirectory = false
        isInstalledSuccessfully = false
        statusMessage = "awaiting IO worker ..."
        val gameBinaryFile = directInstallModScreenState.manageDirectModsScreenState.manageModsScreenState.modManagerScreenState.requireGameBinaryFile()
        withContext(Dispatchers.IO) {
            statusMessage = "preparing ..."

            val userDir = jFile(System.getProperty("user.dir"))
            val installDir = jFile(userDir.absolutePath + "\\MLToolboxApp\\temp\\ue_pak_mods_install")
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
                }){
                    isLoading = false
                    isInvalidModsDirectory = true
                    statusMessage = "unable to lock ue_pak_mods_install directory from app directory, ${lockedFile?.let {
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
                                    isInvalidModsDirectory = true
                                    statusMessage = "unable to delete ${f.toFile().let {
                                        it.absolutePath
                                            .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                                            .replace(' ', '\u00A0')
                                    }} from app directory, it might be opened in another process"
                                    return@withContext
                                }
                                else -> throw e
                            }
                        }
                    }

            statusMessage = "extracting ..."
            mods.forEach { file ->
                val dest = jFile(System.getProperty("user.dir") + "\\MLToolboxApp\\temp\\ue_pak_mods_install\\${file.name}")
                if (file.extension.equals("rar", ignoreCase = true)) {
                    runCatching {
                        dest.mkdirs()
                        Junrar.extract(file, dest)
                    }.onFailure { junrarExtractException ->
                        when (junrarExtractException) {
                            is UnsupportedRarV5Exception -> {
                                runCatching {
                                    RandomAccessFile(file, "r").use { r ->
                                        RandomAccessFileInStream(r).use { sevenZipR ->
                                            SevenZip
                                                .openInArchive(ArchiveFormat.RAR5, sevenZipR)
                                                .use { inArchive ->
                                                    inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                                                        RandomAccessFile(jFile("${dest.absolutePath}\\${archiveItem.path}"), "rw").use {
                                                            archiveItem.extractSlow(RandomAccessFileOutStream(it))
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }.onFailure { sevenZipExtractException ->
                                    if (sevenZipExtractException is SevenZipException) {
                                        isLoading = false
                                        isLastSelectedArchiveInvalid = true
                                        statusMessage = "unable to extract rar5 archive: ${file.name}"
                                        return@withContext
                                    } else if (sevenZipExtractException is IOException) {
                                        isLoading = false
                                        isLastSelectedArchiveInvalid = true
                                        statusMessage = "unable to extract rar5 archive: ${file.name} (IO ERROR)"
                                        return@withContext
                                    }
                                    throw sevenZipExtractException
                                }
                            }
                            is RarException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract rar archive: ${file.name}"
                                return@withContext
                            }
                            is IOException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract rar archive: ${file.name} (IO ERROR)"
                                return@withContext
                            }
                            else -> {
                                throw junrarExtractException
                            }
                        }
                    }
                } else if (file.extension.equals("7z", ignoreCase = true)) {
                    runCatching {
                        dest.mkdirs()
                        RandomAccessFile(file, "r").use { r ->
                            RandomAccessFileInStream(r).use { sevenZipR ->
                                SevenZip
                                    .openInArchive(ArchiveFormat.SEVEN_ZIP, sevenZipR)
                                    .use { inArchive ->
                                        inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                                            RandomAccessFile(jFile("${dest.absolutePath}\\${archiveItem.path}"), "rw").use {
                                                archiveItem.extractSlow(RandomAccessFileOutStream(it))
                                            }
                                        }
                                    }
                            }
                        }
                    }.onFailure { sevenZipExtractException ->
                        sevenZipExtractException.printStackTrace()
                        if (sevenZipExtractException is SevenZipException) {
                            isLoading = false
                            isLastSelectedArchiveInvalid = true
                            statusMessage = "unable to extract 7z archive: ${file.name}"
                            return@withContext
                        } else if (sevenZipExtractException is IOException) {
                            isLoading = false
                            isLastSelectedArchiveInvalid = true
                            statusMessage = "unable to extract 7z archive: ${file.name} (IO ERROR)"
                            return@withContext
                        }
                        throw sevenZipExtractException
                    }
                } else {
                    runCatching {
                        ZipFile(file).use { zipFile ->
                            zipFile.extractAll(dest.absolutePath)
                        }
                    }.onFailure { ex ->
                        when (ex) {
                            is RarException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract zip archive: ${file.name}"
                                return@withContext
                            }
                            is IOException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract zip archive: ${file.name} (IO ERROR)"
                                return@withContext
                            }
                            else -> {
                                throw ex
                            }
                        }
                    }
                }
            }

            statusMessage = "verifying mods ..."
            val modsInstallDir = System.getProperty("user.dir") + "\\MLToolboxApp\\temp\\ue_pak_mods_install"
            val listModsToBeInstalledArchiveDir = jFile(modsInstallDir).listFiles { dir, name ->
                return@listFiles jFile("$dir\\$name").isDirectory
            }
            val listModsToBeInstalled = mutableListOf<jFile>()
            if (listModsToBeInstalledArchiveDir == null) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "error listing ue_pak_mods_install"
                return@withContext
            }
            listModsToBeInstalledArchiveDir.ifEmpty {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "ue_pak_mods_install does not contain any folder"
                return@withContext
            }.forEach { archiveFile ->
                val listFiles = archiveFile.listFiles()
                if (listFiles == null) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "error listing ue_pak_mods_install\\${archiveFile.name}"
                    return@withContext
                }
                if (listFiles.isEmpty() || run {
                    val singleDir = listFiles.filter { it.isDirectory }.size == 1
                    val singleFile = listFiles.filter { it.isFile }.size == 1
                    !(singleFile xor singleDir)
                }) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "'${archiveFile.name}' must only contain one root file or one root directory, given archive is not a Pak mod"
                    return@withContext
                }
                val file = run {
                    val root = listFiles.first()
                    if (root.isFile) return@run root
                    val rootFiles = root.listFiles()
                    if (rootFiles == null) {
                        isLoading = false
                        isLastSelectedArchiveInvalid = true
                        statusMessage = "error listing ue_pak_mods_install\\${root.let {
                            it.absolutePath
                                .drop(it.absolutePath.indexOf(modsInstallDir)+modsInstallDir.length)
                                .replace(' ', '\u00A0')
                        }}"
                        return@withContext
                    }
                    if (rootFiles.isEmpty() || rootFiles.size > 1) {
                        isLoading = false
                        isLastSelectedArchiveInvalid = true
                        statusMessage = "'${archiveFile.name}' must only contain one root file or one root directory with single file, given archive is not a Pak mod"
                        return@withContext
                    }
                    rootFiles.first()
                }
                listModsToBeInstalled.add(file)
                if (file.extension.equals("pak", true))
                    return@forEach
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "'${file.name}' is missing entry point '*.pak', given archive is not a Pak mod"
                return@withContext
            }


            statusMessage = "verifying target game dir ..."
            val gameDir = gameBinaryFile?.parentFile
            if (gameDir == null || !gameDir.exists()) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "missing game directory"
                return@withContext
            }

            val (unrealGameRoot, gameRoot) = resolveGameRoot(gameBinaryFile)
            val paksDir = jFile("$gameRoot\\Content\\Paks")
            if (!paksDir.exists() || !paksDir.isDirectory) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "missing $gameRoot\\Content\\Paks directory"
                return@withContext
            }

            val modsDir = jFile("$gameRoot\\Content\\Paks\\~mods")
            if (!modsDir.exists()) {
                if (!modsDir.mkdir()) {
                    isLoading = false
                    isInvalidModsDirectory = true
                    statusMessage = "unable to create $gameRoot\\Content\\Paks directory"
                    return@withContext
                }
            }

            statusMessage = "verifying target game Mods dir ..."

            val existingFiles = modsDir.listFiles { dir, name ->
                jFile("$dir\\$name").isFile
            }

            if (existingFiles == null) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "error listing $modsDir"
                return@withContext
            }

            val filesToOverwrite = existingFiles.filter { dir ->
                listModsToBeInstalled.any { it.name == dir.name }
            }

            var lockedFile: jFile? = null
            if (!run {
                var open = true
                open = filesToOverwrite.all { file ->
                    var fileOpen = true
                    file.toPath().walk(PathWalkOption.INCLUDE_DIRECTORIES).forEach { f ->
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
                                lockedFile = f.toFile()
                                fileOpen = false
                            } finally {
                                ch?.close()
                            }
                        }
                    }
                    fileOpen
                }
                open
            }) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "unable to lock ${lockedFile!!.let {
                    it.absolutePath
                        .drop(it.absolutePath.indexOf(paksDir.absolutePath)+paksDir.absolutePath.length)
                        .replace(' ', '\u00A0')
                }} from game paks directory, it might be opened in another process"
                return@withContext
            }

            statusMessage = "preparing target game Mods dir ..."

            filesToOverwrite.forEach { dir ->
                dir.toPath()
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
                                    isInvalidModsDirectory = true
                                    statusMessage = "unable to delete ${f.toFile().let {
                                        it.absolutePath
                                            .drop(it.absolutePath.indexOf(paksDir.absolutePath)+paksDir.absolutePath.length)
                                            .replace(' ', '\u00A0')
                                    }} from game paks directory, it might be opened in another process"
                                    return@withContext
                                }
                            }
                        }
                    }
            }

            statusMessage = "installing ..."
            runCatching {
                listModsToBeInstalled.forEach {
                    it.toPath().copyToRecursively(
                        target = jFile("$modsDir\\${it.name}").toPath(),
                        followLinks = false,
                        overwrite = true
                    )
                }
            }.onFailure { ex ->
                isLoading = false
                isInvalidModsDirectory = true
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

    private fun resolveGameRoot(targetGameBinary: jFile): Pair<String, String> {
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