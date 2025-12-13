package dev.psiae.mltoolbox.foundation.fs.internal

import dev.psiae.mltoolbox.foundation.fs.FileSystemException
import dev.psiae.mltoolbox.foundation.fs.path.Path

internal class IllegalFileNameException(
    file: Path,
    other: Path?,
    message: String?
) : FileSystemException(file, other, message) {

    constructor(file: Path) : this(file, null, null)
}