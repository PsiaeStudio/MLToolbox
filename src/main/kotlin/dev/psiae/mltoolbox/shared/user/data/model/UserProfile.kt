package dev.psiae.mltoolbox.shared.user.data.model

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.utils.toBoolean
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.uuid.Uuid

typealias SQLDelightUserProfileEntity = dev.psiae.mltoolbox.data.sqldelight.user.UserProfile

data class UserProfile(
    val id: Long,
    val uuid: Uuid,
    val name: String,
    val isDoneSetup: Boolean,
    val isTombstoned : Boolean,
) {

    companion object {

        fun dir(uid: String): Path = Path(MLToolboxApp.userDir.path) / Path("profiles") / Path(uid)

        fun from(userProfile: SQLDelightUserProfileEntity): UserProfile {
            return UserProfile(
                userProfile.id,
                Uuid.parse(userProfile.uuid),
                userProfile.name,
                userProfile.is_done_setup.toBoolean(),
                userProfile.is_tombstoned.toBoolean()
            )
        }
    }
}