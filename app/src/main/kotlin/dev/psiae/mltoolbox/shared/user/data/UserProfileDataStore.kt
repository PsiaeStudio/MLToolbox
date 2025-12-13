package dev.psiae.mltoolbox.shared.user.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import dev.psiae.mltoolbox.shared.user.data.model.Profile
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting
import dev.psiae.mltoolbox.core.LazyConstructor
import dev.psiae.mltoolbox.core.valueOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.io.path.div

class UserProfileDataStore(

) {
    private val mtx = Mutex()
    private val settingProtoDataStoreMap = mutableMapOf<String, DataStore<UserProfileSetting>>()

    private suspend fun needSettingDataStore(uid: String): DataStore<UserProfileSetting> {
        settingProtoDataStoreMap[uid]?.let { return it }
        return withContext(Dispatchers.IO) {
            val datastore = DataStoreFactory.create(
                serializer = UserProfileSetting.JetpackDataStoreSerializer,
                produceFile = {
                    // note: accidentally wrote `setting.proto`
                    val profileDir = Profile.dir(uid)
                    val file = profileDir / Path("setting.pb")
                    file.toFile()
                }
            )
            settingProtoDataStoreMap[uid] = datastore
            datastore
        }
    }

    suspend fun observeSettingForUser(uid: String): Flow<UserProfileSetting> {
        return mtx.withLock {
            settingProtoDataStoreMap[uid]?.data
                ?: needSettingDataStore(uid).data
        }
    }

    suspend fun settingForUser(uid: String): UserProfileSetting {
        return observeSettingForUser(uid).first()
    }

    suspend fun changeUserProfileTheme(
        uid: String,
        getTheme: (UserProfileSetting.Personalization.Theme) -> UserProfileSetting.Personalization.Theme
    ) {
        mtx.withLock {
            settingProtoDataStoreMap[uid]?.let { dataStore ->
                dataStore.updateData { o ->
                    o.copy(
                        personalization = o.personalization.copy(
                            theme = getTheme(o.personalization.theme)
                        )
                    )
                }
            }
        }
    }

    suspend fun changeUserProfileColorMode(uid: String, colorMode: String) {
        changeUserProfileTheme(uid) { theme -> theme.copy(colorMode = colorMode) }
    }
    suspend fun changeUserProfileColorSeed(uid: String, colorSeed: String) {
        changeUserProfileTheme(uid) { theme -> theme.copy(colorSeed = colorSeed) }
    }



    companion object {
        private val INSTANCE = LazyConstructor<UserProfileDataStore>()

        fun construct() = INSTANCE.constructOrThrow(
            lazyValue = { UserProfileDataStore() },
            lazyThrow = { error("UserRepository already initialized") }
        )

        fun requireInstance(): UserProfileDataStore = INSTANCE.valueOrNull()
            ?: error("UserProfileDataStore not initialized")
    }
}