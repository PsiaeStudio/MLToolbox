package dev.psiae.mltoolbox.feature.modmanager.ui.composeui.managemods.direct

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString

@Composable
fun rememberDirectInstallModScreenState(
    manageDirectModsScreenState: ManageDirectModsScreenState
): DirectInstallModScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(manageDirectModsScreenState) {
        DirectInstallModScreenState(manageDirectModsScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        manageDirectModsScreenState.installModScreenEnter(state)
        onDispose {
            state.stateExit()
            manageDirectModsScreenState.installModScreenExit(state)
        }
    }

    return state
}

class DirectInstallModScreenState(
    val manageDirectModsScreenState: ManageDirectModsScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    var navigateToInstallUE4SS by mutableStateOf(false)
        private set

    var navigateToInstallUE4SSMod by mutableStateOf(false)
        private set

    var navigateToInstallUnrealEngineMod by mutableStateOf(false)
        private set

    private var _isUe4ssInstalled by mutableStateOf(false)

    val isUe4ssInstalled: Boolean
        get() = _isUe4ssInstalled

    private suspend fun isUe4ssInstalled(): Boolean {
        val binaryFile = manageDirectModsScreenState.manageModsScreenState.modManagerScreenState.gameBinaryFile?.toPath()?.takeIf { it.isRegularFile() }
            ?: return false
        val binaryFolder = binaryFile.parent?.takeIf { it.isDirectory() }
            ?: return false
        val proxyDll = binaryFolder.resolve("dwmapi.dll").takeIf { it.isRegularFile() }
            ?: return false
        val ue4ssFolder = binaryFolder.resolve("ue4ss").takeIf { it.isDirectory() }
            ?: return false
        val ue4ssDll = ue4ssFolder.resolve("UE4SS.dll").takeIf { it.isRegularFile() }
            ?: return false
        val modsFolder = ue4ssFolder.resolve("Mods").takeIf { it.isDirectory() }
            ?: return false
        return true
    }

    fun init() {
        coroutineScope.launch {
            launch {
                while (currentCoroutineContext().isActive) {
                    _isUe4ssInstalled = isUe4ssInstalled()
                    delay(1000)
                }
            }
        }
    }

    fun userInputExit() {
        manageDirectModsScreenState.userInputNavigateOutInstallModScreen()
    }

    fun userInputNavigateToInstallUE4SS() {
        navigateToInstallUE4SS = true
    }

    fun userInputNavigateOutInstallUE4SS() {
        navigateToInstallUE4SS = false
    }

    fun userInputNavigateToInstallUE4SSMod() {
        navigateToInstallUE4SSMod = true
    }

    fun userInputNavigateOutInstallUE4SSMod() {
        navigateToInstallUE4SSMod = false
    }

    fun userInputNavigateToInstallUnrealEngineMod() {
        navigateToInstallUnrealEngineMod = true
    }

    fun userInputNavigateOutInstallUnrealEngineMod() {
        navigateToInstallUnrealEngineMod = false
    }
}