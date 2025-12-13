package dev.psiae.mltoolbox.feature.modmanager.ui.managemods

data class InstalledModUiModel(
    val isUE4SS: Boolean,
    val isUEPak: Boolean,
    val name: String,
    val isEnabled: Boolean,
    val qualifiedName: String
) {

    val uniqueQualifiedName = buildString {
        if (isUE4SS) {
            append("ue4ss")
            append("_")
        } else if (isUEPak) {
            append("uepak")
            append("_")
        }
        append(qualifiedName)
    }
}