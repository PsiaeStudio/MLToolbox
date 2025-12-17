package dev.psiae.mltoolbox.shared.domain.model

sealed class ManorLordsGameVersion(
    versionStr: String
) : GameVersion(versionStr) {
    data object V_0_8_029a : ManorLordsGameVersion("0.8.029a")
    data object V_0_8_032 : ManorLordsGameVersion("0.8.032")
    data object V_0_8_050 : ManorLordsGameVersion("0.8.050")
    data class Custom(val customVersionStr: String) : ManorLordsGameVersion("custom")
}