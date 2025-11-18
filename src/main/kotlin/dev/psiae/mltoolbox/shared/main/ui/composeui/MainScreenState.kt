package dev.psiae.mltoolbox.shared.main.ui.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import dev.psiae.mltoolbox.shared.ui.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.shared.ui.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.shared.user.data.model.UserProfile
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting
import dev.psiae.mltoolbox.shared.user.domain.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch

@Composable
fun rememberMainScreenState(): MainScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember { MainScreenState(uiContext) }
    return state
}

@Stable
class MainScreenState(
    val uiContext: ComposeUIContext,
    val userService: UserService = UserService.requireInstance()
) : RememberObserver {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var openSetting by mutableStateOf(false)
        private set

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    var userProfileSetting by mutableStateOf<UserProfileSetting?>(null)
        private set

    val isReady by derivedStateOf {
        userProfile != null && userProfileSetting != null
    }

    val isThemeDark by derivedStateOf {
        val setting = userProfileSetting
            ?: return@derivedStateOf false
        when (setting.personalization.theme.colorMode) {
            UserProfileSetting.Personalization.Theme.COLOR_MODE_LIGHT -> false
            UserProfileSetting.Personalization.Theme.COLOR_MODE_DARK -> true
            UserProfileSetting.Personalization.Theme.COLOR_MODE_SYSTEM -> GLOBAL_IS_SYSTEM_DARK_THEME
            else -> error("Unknown color mode '${setting.personalization.theme.colorMode}'")
        }
    }

    fun stateEnter() {
        _coroutineScope = uiContext.newUICoroutineScope()
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    override fun onAbandoned() {
    }

    override fun onForgotten() {
        stateExit()
    }

    override fun onRemembered() {
        stateEnter()
    }

    fun userInputOpenSetting() {
        openSetting = true
    }

    fun userInputCloseSetting() {
        openSetting = false
    }

    fun init() {
        coroutineScope.launch {
            userService.observeCurrentUserProfile()
                .dropWhile { it == null }
                .collectLatest { profile ->
                    updateState {
                        userProfile = profile
                        userProfileSetting = null
                    }
                }
        }
        coroutineScope.launch {
            var latestProfileCollector: Job? = null
            snapshotFlow { userProfile }
                .dropWhile { it == null }
                .collectLatest { profile ->
                    latestProfileCollector?.cancel()
                    latestProfileCollector = null
                    profile?.let {
                        latestProfileCollector = coroutineScope.launch {
                            userService.observeUserProfileSetting(profile.uuid.toString())
                                .collectLatest { setting ->
                                    updateState {
                                        if (userProfile?.id == profile.id) {
                                            userProfileSetting = setting
                                        }
                                    }
                                }
                        }
                    }
                }
        }
    }

    private inline fun updateState(
        block: MainScreenState.() -> Unit
    ) {
        block()
    }
}