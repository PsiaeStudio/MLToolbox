package dev.psiae.mltoolbox.shared.user.data

import dev.psiae.mltoolbox.shared.user.data.dao.UserProfileDAO
import dev.psiae.mltoolbox.shared.user.data.model.Profile
import dev.psiae.mltoolbox.core.LazyConstructor
import dev.psiae.mltoolbox.core.valueOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.uuid.Uuid

class UserRepository(
    val userProfileDAO: UserProfileDAO,
    val userProfileDataStore: UserProfileDataStore
) {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    suspend fun getAllUserProfile(): List<Profile> {
        return userProfileDAO.getAll()
    }

    suspend fun createDefaultUserProfile() {
        userProfileDAO.insert(
            Profile(
                0,
                Uuid.generateV7(),
                "New User",
                isDoneSetup = false,
                isTombstoned = false
            )
        )
    }

    companion object {
        private val INSTANCE = LazyConstructor<UserRepository>()

        fun construct() = INSTANCE.constructOrThrow(
            lazyValue = { UserRepository(
                UserProfileDAO.requireInstance(),
                UserProfileDataStore.requireInstance()
            ) },
            lazyThrow = { error("UserRepository already initialized") }
        )

        fun requireInstance(): UserRepository = INSTANCE.valueOrNull()
            ?: error("UserRepository not initialized")
    }
}