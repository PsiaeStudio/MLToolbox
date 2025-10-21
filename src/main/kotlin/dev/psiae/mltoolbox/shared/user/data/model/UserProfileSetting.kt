package dev.psiae.mltoolbox.shared.user.data.model

import androidx.datastore.core.CorruptionException
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

import dev.psiae.mltoolbox.data.proto.user.profile.UserProfileSetting as ProtoUserProfileSetting

data class UserProfileSetting(
    val personalization: Personalization = Personalization()
) {
    data class Personalization(
        val theme: Theme = Theme()
    ){
        data class Theme(
            val designSystem: String = DESIGN_SYSTEM_MD3,
            val colorMode: String = COLOR_MODE_SYSTEM,
            val colorSeed: String = COLOR_SEED_GREEN,
            val fontFamily: String = FONT_FAMILY_ROBOTO,
        ) {

            companion object {
                val DESIGN_SYSTEM_MD3 = "google_material_3"

                val COLOR_MODE_LIGHT = "light"
                val COLOR_MODE_DARK = "dark"
                val COLOR_MODE_SYSTEM = "system"

                val COLOR_SEED_GREEN = "green"
                val COLOR_SEED_ORANGE = "orange"
                val COLOR_SEED_BLUE = "blue"
                val COLOR_SEED_YELLOW = "yellow"
                val COLOR_SEED_PURPLE = "purple"

                val FONT_FAMILY_ROBOTO = "roboto"
            }
        }
    }

    companion object {
        val defaultInstance = UserProfileSetting()

        fun from(proto: ProtoUserProfileSetting): UserProfileSetting {
            return UserProfileSetting(
                Personalization(
                    Personalization.Theme(
                        designSystem = proto.personalization.theme.designSystem,
                        colorMode = proto.personalization.theme.colorMode,
                        colorSeed = proto.personalization.theme.colorSeed,
                        fontFamily = proto.personalization.theme.fontFamily,
                    )
                )
            )
        }

        fun UserProfileSetting.toProto(): ProtoUserProfileSetting {
            val personalization = run {
                val theme = run {
                    ProtoUserProfileSetting.Personalization.Theme.newBuilder()
                        .setDesignSystem(personalization.theme.designSystem)
                        .setColorMode(personalization.theme.colorMode)
                        .setColorSeed(personalization.theme.colorSeed)
                        .setFontFamily(personalization.theme.fontFamily)
                        .build()
                }
                ProtoUserProfileSetting.Personalization.newBuilder()
                    .setTheme(theme)
                    .build()
            }
            return ProtoUserProfileSetting.newBuilder()
                .setPersonalization(personalization)
                .build()
        }
    }

    object JetpackDataStoreSerializer : androidx.datastore.core.Serializer<UserProfileSetting> {

        override val defaultValue: UserProfileSetting = UserProfileSetting.defaultInstance

        override suspend fun readFrom(input: InputStream): UserProfileSetting {
            return try {
                UserProfileSetting.from(ProtoUserProfileSetting.parseFrom(input))
            } catch (e: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", e)
            }
        }

        override suspend fun writeTo(
            t: UserProfileSetting,
            output: OutputStream
        ) {
            t.toProto().writeTo(output)
        }
    }
}