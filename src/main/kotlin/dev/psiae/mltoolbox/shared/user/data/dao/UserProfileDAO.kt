package dev.psiae.mltoolbox.shared.user.data.dao

import dev.psiae.mltoolbox.data.Database
import dev.psiae.mltoolbox.shared.data.db.SqlDelightDatabaseProvider
import dev.psiae.mltoolbox.shared.user.data.model.UserProfile
import dev.psiae.mltoolbox.shared.utils.LazyConstructor
import dev.psiae.mltoolbox.shared.utils.UuidV7
import dev.psiae.mltoolbox.shared.utils.toLong
import dev.psiae.mltoolbox.shared.utils.valueOrNull
import kotlin.uuid.Uuid

class UserProfileDAO(
    val db: Database
) {

    fun getAll(): List<UserProfile> {
        return db.userProfileQueries.selectAll().executeAsList().map(UserProfile::from)
    }

    fun findByUUID(uuid: Uuid): UserProfile? {
        return db.userProfileQueries.selectByUuid(uuid.toString()).executeAsOneOrNull()?.let(UserProfile::from)
    }

    fun insert(
        userProfile: UserProfile
    ) {
        val generateUUID = run {
            var result = UuidV7.generate();
            while (true) {
                val exist = findByUUID(result) != null
                if (exist)
                    result = UuidV7.generate();
                else
                    break
            }
            result
        }
        db.userProfileQueries.insert(
            userProfile.id,
            generateUUID.toString(),
            userProfile.name,
            userProfile.isDoneSetup.toLong(),
            userProfile.isTombstoned.toLong()
        )
    }

    companion object {
        private val INSTANCE = LazyConstructor<UserProfileDAO>()

        fun construct() = INSTANCE.constructOrThrow(
            lazyValue = { UserProfileDAO(SqlDelightDatabaseProvider.get()) },
            lazyThrow = { error("UserRepository already initialized") }
        )

        fun requireInstance(): UserProfileDAO = INSTANCE.valueOrNull()
            ?: error("UserRepository not initialized")
    }
}