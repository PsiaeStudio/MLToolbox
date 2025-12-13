package dev.psiae.mltoolbox.shared.domain.model

sealed class GamePlatform(
    val id: String,
    val label: String
) {
    data object Steam : GamePlatform(
        "steam",
        "Steam"
    )
    data object XboxPcGamePass : GamePlatform(
        "xbox_pc_game_pass",
        "Xbox PC Game Pass"
    )
    data object EpicGamesStore : GamePlatform(
        "epic_games_store",
        "Epic Games Store"
    )
    data object GogCom : GamePlatform(
        "gog_com",
        "GOG.com"
    )
}

sealed class OsPlatform(
    val id: String,
    val label: String
) {
    sealed class Windows(id: String, label: String) : OsPlatform(id, label) {
        data object Windows10 : Windows("win10", "Windows 10")
        data object Windows11 : Windows("win11", "Windows 11")
    }
}

sealed class DevicePlatform(
    val id: String,
    val label: String
) {
    sealed class PC : DevicePlatform("pc", "PC")
}