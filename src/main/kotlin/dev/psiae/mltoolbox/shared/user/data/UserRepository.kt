package dev.psiae.mltoolbox.shared.user.data

import dev.psiae.mltoolbox.shared.user.data.dao.UserProfileDAO
import dev.psiae.mltoolbox.shared.user.data.model.UserProfile
import dev.psiae.mltoolbox.shared.utils.LazyConstructor
import dev.psiae.mltoolbox.shared.utils.valueOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.uuid.Uuid

class UserRepository(
    val userProfileDAO: UserProfileDAO,
    val userProfileDataStore: UserProfileDataStore
) {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    suspend fun getAllUserProfile(): List<UserProfile> {
        return userProfileDAO.getAll()
    }

    suspend fun createDefaultUserProfile() {
        userProfileDAO.insert(
            UserProfile(
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