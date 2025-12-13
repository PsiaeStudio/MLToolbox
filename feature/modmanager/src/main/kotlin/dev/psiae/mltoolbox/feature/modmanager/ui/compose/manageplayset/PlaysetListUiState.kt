package dev.psiae.mltoolbox.feature.modmanager.ui.compose.manageplayset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.feature.modmanager.domain.model.ModManagerGameContext
import dev.psiae.mltoolbox.feature.modmanager.ui.managemods.InstalledModUiModel
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.ui.compose.DispatchContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.collections.ifEmpty
import kotlin.io.path.bufferedWriter
import kotlin.io.path.useLines

@Composable
fun rememberPlaysetListUiState(
    dispatchContext: DispatchContext,
    gameContext: ModManagerGameContext
): PlaysetListUiState {
    val coroutineScope = rememberCoroutineScope()
    val state = remember {
        PlaysetListUiState(coroutineScope, dispatchContext, gameContext)
    }
    remember {
        object : RememberObserver {
            override fun onAbandoned() {
            }
            override fun onForgotten() {
            }
            override fun onRemembered() {
                state.init()
            }
        }
    }
    return state
}

class PlaysetListUiState(
    val coroutineScope: CoroutineScope,
    val dispatchContext: DispatchContext,
    val gameContext: ModManagerGameContext
) {

    val fs = FileSystem.SYSTEM

    private val mainDispatcher
        get() = dispatchContext.mainDispatcher

    fun init() {
        coroutineScope.launch(mainDispatcher.immediate) {
            while (true) {
                ensureActive()
                refreshSuspend()
                delay(1000)
            }
        }
    }

    fun dispose() {

    }

    suspend fun refreshSuspend() {
        try { updateInstalledPlaysetList?.cancelAndJoin() } catch (_: CancellationException) {}
        currentCoroutineContext().ensureActive()
        updateInstalledPlaysetList = coroutineScope.launch { doUpdateInstalledPlaysetList() }
        updateInstalledPlaysetList?.join()
    }

    private suspend fun doUpdateInstalledPlaysetList() {
        val gameFile = gameContext.paths.binary
        var result: List<InstalledModUiModel> = emptyList()
        withContext(Dispatchers.IO) {
            runCatching {
                val ue4ssMods: List<InstalledModUiModel> = run {
                    val modsFolder = gameFile.parent
                        ?.let { fs.file(it / "ue4ss" / "Mods") }
                        ?: return@run emptyList()
                    if (!modsFolder.exists() || !modsFolder.followLinks().isDirectory())
                        return@run emptyList()
                    val modPaths = fs.list(modsFolder.path).filter { path ->
                        val file = fs.file(path)
                        file.exists() && file.followLinks().isDirectory() &&
                                fs.file(path / "dlls" / "main.dll").let { it.exists() && it.followLinks().isRegularFile() } ||
                                fs.file(path / "Scripts" / "main.lua").let { it.exists() && it.followLinks().isRegularFile() }
                    }.ifEmpty {
                        return@run emptyList()
                    }
                    processToInstalledUE4SSModData(modsFolder.path, modPaths)
                }
                val pakMods: List<InstalledModUiModel> = run {
                    val pakModsFolder = fs.file(gameContext.paths.paks / "~mods")
                    if (!pakModsFolder.exists() || !pakModsFolder.followLinks().isDirectory()) {
                        return@run emptyList()
                    }
                    val mods = fs.list(pakModsFolder.path).filter { e ->
                        fs.file(e).let { it.exists() && it.followLinks().isRegularFile() } && e.name.endsWith(".pak")
                    }
                    processToInstalledPakModData(pakModsFolder.path, mods)
                }
                result = mutableListOf<InstalledModUiModel>()
                    .apply {
                        addAll(ue4ssMods)
                        addAll(pakMods)
                    }
            }.catchOrRethrow { e ->
                runCatching { e.printStackTrace() }
                if (e is IOException)
                    return@withContext
            }
        }
        this.installedPlaysetList = result
        this.filteredInstalledPlaysetList = this.installedPlaysetList
        if (queryParamsEnabled) {
            queryChange(lastQuery, reQuery = true)
        }
    }

    private fun processToInstalledUE4SSModData(
        modsDirPath: Path,
        modPaths: List<Path>
    ): List<InstalledModUiModel> {
        val modsTxtPath = modsDirPath / "mods.txt"
        val modsTxtFile = fs.file(modsTxtPath)
        return modPaths.map { mod ->
            val modName = mod.name
            val isEnabled = fs.file(mod / "enabled.txt").let { it.exists() && it.followLinks().isRegularFile() } ||
                run {
                    // https://github.com/UE4SS-RE/RE-UE4SS/blob/8d3857273f12ce8c3800575dee537c5de9d690ef/UE4SS/src/UE4SSProgram.cpp#L1130
                    modsTxtFile.exists() && modsTxtFile.followLinks().isRegularFile() && modsTxtFile.path.toJNioPath().useLines { lineSequence ->
                        lineSequence.any { line ->
                            var mutLine = line
                            if (line.contains(';'))
                                return@any false
                            if (line.length <= 4)
                                return@any false
                            mutLine = mutLine.filterNot { it == ' ' }
                            if (mutLine.isEmpty())
                                return@any false
                            val entryModName = mutLine.takeWhile { it != ':' }
                            if (!entryModName.equals(modName, ignoreCase = true))
                                return@any false
                            val entryModEnabled = mutLine.takeLastWhile { it != ':' }
                            entryModEnabled.isNotEmpty() && entryModEnabled[0] == '1'
                        }
                    }
                }
            InstalledModUiModel(
                isUE4SS = true,
                isUEPak = false,
                name = modName,
                isEnabled = isEnabled,
                qualifiedName = modName
            )
        }
    }

    private fun processToInstalledPakModData(
        modsDirPath: Path,
        modPaths: List<Path>
    ): List<InstalledModUiModel> {
        return modPaths.map { mod ->
            val modName = mod.name.substringBeforeLast(".")
            val isEnabled = true
            InstalledModUiModel(
                isUE4SS = false,
                isUEPak = true,
                name = modName,
                isEnabled = isEnabled,
                qualifiedName = modName
            )
        }
    }

    fun toggleMod(
        mod: InstalledModUiModel
    ) {
        coroutineScope.launch {
            val gameBinary = gameContext.paths.binary
            val gameBinaryFolder = gameBinary.parent ?: return@launch
            withContext(Dispatchers.IO) {
                runCatching {
                    if (mod.isUE4SS) {
                        toggleUE4SSModEnableFlag(
                            mod.name,
                            gameBinaryFolder / "ue4ss" / "Mods"
                        )
                    }
                }.catchOrRethrow { e ->
                    Logger.tryLog { e.stackTraceToString() }
                    if (e is IOException)
                        return@withContext
                }
            }
            refreshSuspend()
        }
    }

    private fun toggleUE4SSModEnableFlag(
        modName: String,
        ue4ssModsDirPath: Path
    ) {
        val hasEnabledTxt = fs.file(ue4ssModsDirPath / modName / "enabled.txt").let { it.exists() && it.followLinks().isRegularFile() }
        fs.file(ue4ssModsDirPath / modName / "enabled.txt").deleteIfExist()
        val modsTxtFile = fs.file(ue4ssModsDirPath / "mods.txt")
        if (modsTxtFile.exists()) {
            if (!modsTxtFile.followLinks().isRegularFile()) {
                modsTxtFile.deleteIfExist()
                fs.createFile(modsTxtFile.path)
            }
        } else {
            fs.createFile(modsTxtFile.path)
        }
        var entryLine = -1
        var entryEnabled = hasEnabledTxt
        val lines = mutableListOf<String>()
        val hasModEntry = run {
            // https://github.com/UE4SS-RE/RE-UE4SS/blob/8d3857273f12ce8c3800575dee537c5de9d690ef/UE4SS/src/UE4SSProgram.cpp#L1130
            modsTxtFile.path.toJNioPath().useLines { lineSequence ->
                var i = -1
                lines.addAll(lineSequence)
                lines.any { line ->
                    i++
                    var mutLine = line
                    if (line.contains(';'))
                        return@any false
                    if (line.length <= 4)
                        return@any false
                    mutLine = mutLine.filterNot { it == ' ' }
                    if (mutLine.isEmpty())
                        return@any false
                    val entryModName = mutLine.takeWhile { it != ':' }
                    entryModName.equals(modName, ignoreCase = true).also { isTheModEntry ->
                        if (isTheModEntry) {
                            entryLine = i
                            val entryModEnabledField = mutLine.takeLastWhile { it != ':' }
                            entryEnabled = hasEnabledTxt || entryModEnabledField.isNotEmpty() && entryModEnabledField[0] == '1'
                        }
                    }
                }
            }
        }
        if (hasModEntry) {
            lines[entryLine] = buildString {
                append(modName)
                append(' ')
                append(':')
                append(' ')
                append(if (entryEnabled) "0" else "1")
            }
            if (entryLine < lines.lastIndex) {
                // remove duplicates
                lines
                    .subList(entryLine+1, lines.size)
                    .removeAll { line ->
                        line
                            .dropWhile { it == ' ' }
                            .takeWhile { it != ':' }
                            .dropLastWhile { it == ' ' }
                            .equals(modName, ignoreCase = true)
                    }
            }
        } else {
            lines.add(0, buildString {
                append(modName)
                append(':')
                append(' ')
                append(if (entryEnabled) "0" else "1")
            })
        }
        with(modsTxtFile.path.toJNioPath().bufferedWriter()) {
            lines.forEachIndexed { i, e ->
                write(e)
                if (i < lines.lastIndex)
                    newLine()
            }
            flush()
        }
    }

    fun deleteMod(
        mod: InstalledModUiModel
    ) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    if (mod.isUEPak) {
                        deleteUEPakMod(mod)
                    }
                }.catchOrRethrow { e ->
                    if (e is IOException)
                        return@withContext
                }
            }
            refreshSuspend()
        }
    }

    private fun deleteUEPakMod(
        mod: InstalledModUiModel
    ) {
        fs.file(gameContext.paths.paks / "~mods" / "${mod.name}.pak").deleteIfExist()
    }


    var installedPlaysetList by mutableStateOf(
        listOf<InstalledModUiModel>(),
        neverEqualPolicy()
    )

    var filteredInstalledPlaysetList by mutableStateOf(
        listOf<InstalledModUiModel>(),
        neverEqualPolicy()
    )

    var queriedInstalledPlaysetList by mutableStateOf(
        listOf<InstalledModUiModel>(),
        neverEqualPolicy()
    )

    var queryParamsEnabled by mutableStateOf(
        false
    )

    var queryParamsKey by mutableStateOf(Any())
        private set

    var lastQuery by mutableStateOf("")
        private set

    private var updateInstalledPlaysetList: Job? = null

    private var lastQueryTask: Job? = null
    private var lastQueryStamp = 0L
    fun queryChange(
        query: String,
        reQuery: Boolean = false
    ) {
        if (!reQuery && query.equals(lastQuery, ignoreCase = true)) {
            return
        }
        lastQuery = query
        lastQueryTask?.cancel()
        lastQueryTask = coroutineScope.launch {
            if (query.isNotEmpty()) {
                delay(100)
                val src = filteredInstalledPlaysetList
                val queried = withContext(Dispatchers.Default) {
                    src.filterIndexed { i, e ->
                        e.name.indexOf(query, ignoreCase = true) >= 0
                    }
                }
                ensureActive()
                queriedInstalledPlaysetList = queried
                queryParamsKey = Any()
                queryParamsEnabled = true
            } else {
                queriedInstalledPlaysetList = emptyList()
                queryParamsKey = Any()
                queryParamsEnabled = false
            }
        }
    }
}