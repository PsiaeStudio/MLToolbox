package dev.psiae.mltoolbox.feature.modmanager.domain.model

import dev.psiae.mltoolbox.foundation.domain.ConfigurationKey
import dev.psiae.mltoolbox.shared.domain.model.GamePlatform
import dev.psiae.mltoolbox.shared.domain.model.GameVersion

data class ModManagerGameContext(
    val platform: GamePlatform,
    val paths: ModManagerGamePaths,
    val version: GameVersion
) {

    companion object : ConfigurationKey {

        override val displayString: String
            get() = "Mod Manager Game Context"
    }
}