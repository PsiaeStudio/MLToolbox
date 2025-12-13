package dev.psiae.mltoolbox.feature.modmanager.domain.model

import dev.psiae.mltoolbox.foundation.fs.path.Path

data class ModManagerGamePaths(
    val install: Path,
    val root: Path,
    /*val gameRoot: Path,*/
    val launcher: Path,
    val binary: Path,
    val paks: Path,
) {

    companion object {
        val EMPTY = ModManagerGamePaths(
            install = Path.EMPTY,
            root = Path.EMPTY,
            /*gameRoot = Path.Empty,*/
            launcher = Path.EMPTY,
            binary = Path.EMPTY,
            paks = Path.EMPTY,
        )

        fun ModManagerGamePaths?.orEmpty(): ModManagerGamePaths = this ?: EMPTY
    }
}