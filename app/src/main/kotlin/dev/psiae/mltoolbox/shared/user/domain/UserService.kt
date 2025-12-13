package dev.psiae.mltoolbox.shared.user.domain

import dev.psiae.mltoolbox.shared.user.data.UserRepository
import dev.psiae.mltoolbox.shared.user.data.model.Profile
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting
import dev.psiae.mltoolbox.core.LazyConstructor
import dev.psiae.mltoolbox.core.valueOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private val _currentProfileFlow = MutableStateFlow<Profile?>(null)

    private var _isStarted = false
    private var _currentProfile: Profile? = null

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
                _currentProfile = userProfiles.first()
                _currentProfileFlow.value = _currentProfile
            }
            _isStarted = true
        }
    }


    private fun lazyStart() {
        if (!_isStarted) {
            start()
        }
    }


    val currentProfile: Profile?
        get() = _currentProfileFlow.value


    fun observeCurrentUserProfile(): Flow<Profile?> {
        lazyStart()
        return _currentProfileFlow.asStateFlow()
    }

    fun observeUserProfileSetting(uid: String): Flow<UserProfileSetting?> {
        lazyStart()
        return flow {
            emitAll(userRepo.userProfileDataStore.observeSettingForUser(uid))
        }
    }

    fun changeUserProfileColorMode(uid: String, colorMode: String): Deferred<Unit> {
        val def = CompletableDeferred<Unit>()
        _coroutineScope.launch(Dispatchers.IO) {
            userRepo.userProfileDataStore.changeUserProfileColorMode(uid, colorMode)
        }.invokeOnCompletion { cancellationCause ->
            when (cancellationCause) {
                null -> def.complete(Unit)
                is CancellationException -> def.cancel(cancellationCause)
                else -> def.completeExceptionally(cancellationCause)
            }
        }
        return def
    }

    fun changeUserProfileColorSeed(uid: String, colorSeed: String): Deferred<Unit> {
        val def = CompletableDeferred<Unit>()
        _coroutineScope.launch(Dispatchers.IO) {
            userRepo.userProfileDataStore.changeUserProfileColorSeed(uid, colorSeed)
        }.invokeOnCompletion { cancellationCause ->
            when (cancellationCause) {
                null -> def.complete(Unit)
                is CancellationException -> def.cancel(cancellationCause)
                else -> def.completeExceptionally(cancellationCause)
            }
        }
        return def
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