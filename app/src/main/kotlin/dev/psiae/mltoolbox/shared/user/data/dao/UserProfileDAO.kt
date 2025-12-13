package dev.psiae.mltoolbox.shared.user.data.dao

import dev.psiae.mltoolbox.data.sqldelight.AppDatabase
import dev.psiae.mltoolbox.shared.data.db.SqlDelightDatabaseProvider
import dev.psiae.mltoolbox.shared.user.data.model.Profile
import dev.psiae.mltoolbox.core.LazyConstructor
import dev.psiae.mltoolbox.shared.utils.UuidV7
import dev.psiae.mltoolbox.shared.utils.toLong
import dev.psiae.mltoolbox.core.valueOrNull
import kotlin.uuid.Uuid

class UserProfileDAO(
    val db: AppDatabase
) {

    fun getAll(): List<Profile> {
        return db.profileQueries.selectAll().executeAsList().map(Profile::from)
    }

    fun findByUUID(uuid: Uuid): Profile? {
        return db.profileQueries.selectByUuid(uuid.toString()).executeAsOneOrNull()?.let(Profile::from)
    }

    fun insert(
        profile: Profile
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
        db.profileQueries.insert(
            profile.id,
            generateUUID.toString(),
            profile.name,
            profile.isDoneSetup.toLong(),
            profile.isTombstoned.toLong()
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