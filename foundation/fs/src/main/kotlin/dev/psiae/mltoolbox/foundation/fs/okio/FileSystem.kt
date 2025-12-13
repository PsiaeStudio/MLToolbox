package dev.psiae.mltoolbox.foundation.fs.okio

import dev.psiae.mltoolbox.foundation.fs.OkioFileSystem
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toOkioPath

fun OkioFileSystem.exists(path: Path): Boolean {
    return exists(path.toOkioPath())
}