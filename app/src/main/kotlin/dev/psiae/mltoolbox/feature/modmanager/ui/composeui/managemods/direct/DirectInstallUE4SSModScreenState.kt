package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.runtime.*
import com.github.junrar.Junrar
import com.github.junrar.exception.RarException
import com.github.junrar.exception.UnsupportedRarV5Exception
import com.sun.nio.file.ExtendedOpenOption
import dev.psiae.mltoolbox.foundation.ui.ScreenContext
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.shared.utils.isNullOrNotActive
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.*
import net.lingala.zip4j.ZipFile
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
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
fun rememberDirectInstallUE4SSModScreenState(
    directInstallModScreenState: DirectInstallModScreenState
): DirectInstallUE4SSModScreenState {
    val composeUIContext = LocalScreenContext.current
    val state = remember(directInstallModScreenState) {
        DirectInstallUE4SSModScreenState(directInstallModScreenState, composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class DirectInstallUE4SSModScreenState(
    val directInstallModScreenState: DirectInstallModScreenState,
    val uiContext: ScreenContext
) {
    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    private var pickUE4SSModsArchiveCompletion: Deferred<List<jFile>?>? = null

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
        _coroutineScope = uiContext.createCoroutineScope()

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
            if (pickUE4SSModsArchiveCompletion?.isActive != true) {
                isLastSelectedArchiveInvalid = false
                isInvalidModsDirectory = false
                isInstalledSuccessfully = false
                statusMessage = null
                val pickTask = async {
                    val pick = FileKit.pickFile(
                        type = PickerType.File(listOf("zip", "rar", "7z")),
                        mode = PickerMode.Multiple(),
                        title = "Select downloaded UE4SS Mod(s) archive (*.zip, *.rar, *.7z)",
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
                }
                pickUE4SSModsArchiveCompletion = pickTask
                runCatching {
                    pickTask.await()
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

    fun userDropUE4SSModsArchive(
        mods: List<jFile>
    ) {
        if (mods.isEmpty()) return
        coroutineScope.launch {
            if (pickUE4SSModsArchiveCompletion.isNullOrNotActive()) {
                val task = async {
                    processSelectedModsArchive(mods)
                    mods
                }
                pickUE4SSModsArchiveCompletion = task
                runCatching {
                    task.await()
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
            val installDir = jFile(userDir.absolutePath + "\\MLToolboxApp\\temp\\ue4ss_mods_install")
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
                    isInvalidModsDirectory = true
                    statusMessage = "unable to lock ue4ss_mods_install directory from app directory, ${lockedFile?.let {
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
                                    }} from app directory, it might be used by another process"
                                    return@withContext
                                }
                                else -> throw e
                            }
                        }
                    }

            statusMessage = "extracting ..."
            mods.forEach { file ->
                val dest = jFile(System.getProperty("user.dir") + "\\MLToolboxApp\\temp\\ue4ss_mods_install\\${file.name}")
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
                                                    val destCanonical = dest.canonicalFile
                                                    inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                                                        val simple = inArchive.simpleInterface
                                                        for (item in simple.archiveItems) {
                                                            val entryPath = item.path ?: continue
                                                            val outFile = jFile(destCanonical, entryPath).canonicalFile

                                                            if (item.isFolder) {
                                                                outFile.mkdirs()
                                                                continue
                                                            }

                                                            outFile.parentFile?.mkdirs()

                                                            RandomAccessFile(outFile, "rw").use { outRaf ->
                                                                outRaf.setLength(0)
                                                                val result = item.extractSlow(RandomAccessFileOutStream(outRaf))
                                                                if (result != ExtractOperationResult.OK) {
                                                                    throw RuntimeException("Extraction failed for ${item.path}: $result")
                                                                }
                                                            }
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
                                        val destCanonical = dest.canonicalFile
                                        inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                                            val simple = inArchive.simpleInterface
                                            for (item in simple.archiveItems) {
                                                val entryPath = item.path ?: continue
                                                val outFile = jFile(destCanonical, entryPath).canonicalFile

                                                if (item.isFolder) {
                                                    outFile.mkdirs()
                                                    continue
                                                }

                                                outFile.parentFile?.mkdirs()

                                                RandomAccessFile(outFile, "rw").use { outRaf ->
                                                    outRaf.setLength(0)
                                                    val result = item.extractSlow(RandomAccessFileOutStream(outRaf))
                                                    if (result != ExtractOperationResult.OK) {
                                                        throw RuntimeException("Extraction failed for ${item.path}: $result")
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }.onFailure { sevenZipExtractException ->
                        if (sevenZipExtractException is SevenZipException) {
                            isLoading = false
                            isLastSelectedArchiveInvalid = true
                            statusMessage = "unable to extract 7z archive: ${file.name}"
                            Logger.log(sevenZipExtractException.stackTraceToString());
                            return@withContext
                        } else if (sevenZipExtractException is IOException) {
                            isLoading = false
                            isLastSelectedArchiveInvalid = true
                            statusMessage = "unable to extract 7z archive: ${file.name} (IO ERROR)"
                            Logger.log(sevenZipExtractException.stackTraceToString());
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
            val modsInstallDir = System.getProperty("user.dir") + "\\MLToolboxApp\\temp\\ue4ss_mods_install"
            val listModsToBeInstalledArchiveDir = jFile(modsInstallDir).listFiles { dir, name ->
                return@listFiles jFile("$dir\\$name").isDirectory
            }
            val listModsToBeInstalledDir = mutableListOf<jFile>()
            if (listModsToBeInstalledArchiveDir == null) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "error listing ue4ss_mods_install"
                return@withContext
            }

            var needLogicModsFolder = false
            listModsToBeInstalledArchiveDir.ifEmpty {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "ue4ss_mods_install does not contain any folder"
                return@withContext
            }.forEach { archiveFile ->
                val listFiles = archiveFile.listFiles()
                if (listFiles == null) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "error listing ue4ss_mods_install\\${archiveFile.name}"
                    return@withContext
                }
                if (listFiles.isEmpty() || listFiles.size > 1 || !listFiles.first().isDirectory) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "'${archiveFile.name}' must only contain one root directory, given archive is not a UE4SS mod"
                    return@withContext
                }
                val file = listFiles.first()
                listModsToBeInstalledDir.add(file)

                val ae_bp = jFile(file, "ae_bp")
                if (ae_bp.exists() && ae_bp.isDirectory) {
                    val modBP = jFile(ae_bp, "${file.name}.pak")
                    if (modBP.exists()) {
                        needLogicModsFolder = true
                    }
                }

                val dllsMain = jFile("$file\\dlls\\main.dll")
                if (dllsMain.exists())
                    return@forEach
                val scriptsMain = jFile("$file\\Scripts\\main.lua")
                if (scriptsMain.exists())
                    return@forEach
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "'${file.name}' is missing entry point (dlls\\main.dll or Scripts\\main.lua), given archive is not a UE4SS mod"
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

            val ue4ssDir = jFile("$gameDir\\ue4ss")
            if (!ue4ssDir.exists() || !ue4ssDir.isDirectory) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "missing ue4ss directory, make sure 'RE-UE4SS' Mod Loader is already installed"
                return@withContext
            }

            statusMessage = "verifying target game Mods dir ..."

            val modsDir = jFile("$ue4ssDir\\Mods")
            if (!modsDir.exists() || !modsDir.isDirectory) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "missing ue4ss\\Mods directory"
                return@withContext
            }

            val existingModDirectories = modsDir.listFiles { dir, name ->
                return@listFiles jFile("$dir\\$name").isDirectory
            }

            if (existingModDirectories == null) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "error listing $modsDir"
                return@withContext
            }

            val directoriesToOverwrite = existingModDirectories.filter { dir ->
                listModsToBeInstalledDir.any { it.name == dir.name }
            }

            var lockedFile: jFile? = null
            if (!run {
                var open = true
                open = directoriesToOverwrite.all { dir ->
                    var dirOpen = true
                    dir.toPath().walk(PathWalkOption.INCLUDE_DIRECTORIES).forEach { f ->
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
                                dirOpen = false
                            } finally {
                                ch?.close()
                            }
                        }
                    }
                    dirOpen
                }
                open
            }) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "unable to lock ${lockedFile!!.let {
                    it.absolutePath
                        .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                        .replace(' ', '\u00A0')
                }} from game directory, it might be opened in another process"
                return@withContext
            }

            statusMessage = "preparing target game Mods dir ..."

            directoriesToOverwrite.forEach { dir ->
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
                        }.onFailure { e ->
                            when (e) {
                                is NoSuchFileException, is DirectoryNotEmptyException, is IOException -> {
                                    isLoading = false
                                    isInvalidModsDirectory = true
                                    statusMessage = "unable to delete ${f.toFile().let {
                                        it.absolutePath
                                            .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                                            .replace(' ', '\u00A0')
                                    }} from game directory, it might be opened in another process"
                                    return@withContext
                                }
                                else -> throw e
                            }
                        }
                    }
            }

            if (needLogicModsFolder) run {
                statusMessage = "preparing LogicMods dir ..."

                val (unrealGameRoot, gameRoot) = gameBinaryFile.absolutePath
                    .split("\\")
                    .let { split ->
                        if (split.size < 5) {
                            isLoading = false
                            isInvalidModsDirectory = true
                            statusMessage = "unable to find target game binary root directory, split size to small=${split.size}"
                            return@withContext
                        }
                        split.dropLast(4).joinToString("\\") to split.dropLast(3).joinToString("\\")
                    }
                val pakDir = jFile("$gameRoot\\Content\\Paks")
                val logicModsDir = jFile(pakDir, "LogicMods")
                if (!logicModsDir.exists() && !logicModsDir.mkdir()) {
                    isLoading = false
                    isInvalidModsDirectory = true
                    statusMessage = "Unable to create LogicMods directory"
                    return@withContext
                }
                if (logicModsDir.exists() && !logicModsDir.isDirectory) {
                    isLoading = false
                    isInvalidModsDirectory = true
                    statusMessage = "LogicMods folder exist but is not a directory"
                    return@withContext
                }

                listModsToBeInstalledDir.forEach { file ->
                    val ae_bp = jFile(file, "ae_bp")
                    if (ae_bp.exists() && ae_bp.isDirectory) {
                        val modBP = jFile(ae_bp, "${file.name}.pak")
                        if (modBP.exists()) {

                            val logicModsDir = jFile("$gameRoot\\Content\\Paks\\LogicMods")
                            if (!logicModsDir.isDirectory) {
                                isLoading = false
                                isInvalidModsDirectory = true
                                statusMessage = "$gameRoot\\Content\\Paks\\LogicMods is not directory"
                                return@withContext
                            }
                            val targetFile = jFile(logicModsDir, modBP.name)
                            if (targetFile.exists()) {
                                if (!targetFile.delete()) {
                                    isLoading = false
                                    isInvalidModsDirectory = true
                                    statusMessage = "unable to delete ${targetFile.let {
                                        it.absolutePath
                                            .replace(' ', '\u00A0')
                                    }}, it might be opened in another process"
                                    return@withContext
                                }
                            }
                        }
                    }
                }
            }


            statusMessage = "installing ..."
            runCatching {
                listModsToBeInstalledDir.forEach {
                    it.toPath().copyToRecursively(
                        target = jFile("$modsDir\\${it.name}").toPath(),
                        followLinks = false,
                        overwrite = false
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
                        "unable to copy recursively, target file already exist"
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


            runCatching {
                val (unrealGameRoot, gameRoot) = gameBinaryFile.absolutePath
                    .split("\\")
                    .let { split ->
                        if (split.size < 5) {
                            isLoading = false
                            isInvalidModsDirectory = true
                            statusMessage = "unable to find target game binary root directory, split size to small=${split.size}"
                            return@withContext
                        }
                        split.dropLast(4).joinToString("\\") to split.dropLast(3).joinToString("\\")
                    }
                listModsToBeInstalledDir
                    .forEach { mod ->
                        val ae_bp = jFile(mod, "ae_bp")
                        if (ae_bp.exists() && ae_bp.isDirectory) {
                            val modBP = jFile(ae_bp, "${mod.name}.pak")
                            if (modBP.exists()) {
                                // move to LogicMods

                                val logicModsDir = jFile("$gameRoot\\Content\\Paks\\LogicMods")
                                if (!logicModsDir.isDirectory) {
                                    isLoading = false
                                    isInvalidModsDirectory = true
                                    statusMessage = "$gameRoot\\Content\\Paks\\LogicMods is not directory"
                                    return@withContext
                                }
                                val targetFile = jFile(logicModsDir, modBP.name)
                                modBP.toPath().copyTo(targetFile.toPath(), overwrite = false)
                            }
                        }
                    }
            }.onFailure { ex ->
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = when (ex) {
                    is FileNotFoundException -> {
                        "unable to copy bp file, source file is missing"
                    }
                    is FileAlreadyExistsException -> {
                        "unable to copy bp file, target file already exist"
                    }
                    is AccessDeniedException -> {
                        "unable to copy bp file, access denied"
                    }
                    is IOException -> {
                        "unable to copy bp file, IO error"
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