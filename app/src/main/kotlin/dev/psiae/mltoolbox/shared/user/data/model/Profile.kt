package dev.psiae.mltoolbox.shared.user.data.model

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.utils.toBoolean
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.uuid.Uuid

typealias SQLDelightProfileEntity = dev.psiae.mltoolbox.data.Profile

data class Profile(
    val id: Long,
    val uuid: Uuid,
    val name: String,
    val isDoneSetup: Boolean,
    val isTombstoned : Boolean,
) {

    companion object {

        fun dir(uid: String): Path = Path(MLToolboxApp.userDir.path) / Path("profiles") / Path(uid)

        fun from(profile: SQLDelightProfileEntity): Profile {
            return Profile(
                profile.id,
                Uuid.parse(profile.uuid),
                profile.name,
                profile.is_done_setup.toBoolean(),
                profile.is_tombstoned.toBoolean()
            )
        }
    }
}