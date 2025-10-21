package dev.psiae.mltoolbox.shared.user.domain

import dev.psiae.mltoolbox.shared.user.data.UserProfileDataStore
import dev.psiae.mltoolbox.shared.user.data.UserRepository
import dev.psiae.mltoolbox.shared.user.data.model.UserProfile
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting
import dev.psiae.mltoolbox.shared.utils.LazyConstructor
import dev.psiae.mltoolbox.shared.utils.valueOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class UserService(
    private val userRepo: UserRepository,
) {

    private val _coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
    private val _currentUserProfileFlow = MutableStateFlow<UserProfile?>(null)

    private var _isStarted = false
    private var _currentUserProfile: UserProfile? = null

    @Synchronized
    private fun start() {
        if (!_isStarted) {
            _coroutineScope.launch(Dispatchers.IO) {
                var userProfiles = userRepo.getAllUserProfile()
                if (userProfiles.isEmpty()) {
                    userRepo.createDefaultUserProfile()
                    userProfiles = userRepo.getAllUserProfile()
                }
                if (userProfiles.isEmpty())
                    error("No user profile")
                _currentUserProfile = userProfiles.first()
                _currentUserProfileFlow.value = _currentUserProfile
            }
            _isStarted = true
        }
    }


    private fun lazyStart() {
        if (!_isStarted) {
            start()
        }
    }


    val currentUserProfile: UserProfile?
        get() = _currentUserProfileFlow.value


    fun observeCurrentUserProfile(): Flow<UserProfile?> {
        lazyStart()
        return _currentUserProfileFlow.asStateFlow()
    }

    fun observeUserProfileSetting(uid: String): Flow<UserProfileSetting?> {
        lazyStart()
        return flow {
            emitAll(userRepo.userProfileDataStore.observeSettingForUser(uid))
        }
    }

    fun changeUserProfileColorMode(uid: String, colorMode: String): Deferred<Unit> {
        return _coroutineScope.async(Dispatchers.IO) {
            userRepo.userProfileDataStore.changeUserProfileColorMode(uid, colorMode)
        }
    }
    fun changeUserProfileColorSeed(uid: String, colorSeed: String): Deferred<Unit> {
        return _coroutineScope.async(Dispatchers.IO) {
            userRepo.userProfileDataStore.changeUserProfileColorSeed(uid, colorSeed)
        }
    }



    companion object {
        private val INSTANCE = LazyConstructor<UserService>()

        fun construct() = INSTANCE.constructOrThrow(
            lazyValue = { UserService(
                UserRepository.requireInstance()
            ) },
            lazyThrow = { error("UserService already initialized") }
        )

        fun requireInstance(): UserService = INSTANCE.valueOrNull()
            ?: error("UserService not initialized")
    }
}