package dev.psiae.mltoolbox.foundation.fs.okio

import dev.psiae.mltoolbox.foundation.fs.path.OkioPath

fun OkioPath.startsWith(
    other: OkioPath,
    normalize: Boolean = true
): Boolean {

    if (normalize)
        normalized().startsWith(other.normalized(), false)

    if (this.root != other.root)
        return false

    if (this.segments.size < other.segments.size)
        return false

    return this.segments.subList(0, other.segments.size) == other.segments
}

