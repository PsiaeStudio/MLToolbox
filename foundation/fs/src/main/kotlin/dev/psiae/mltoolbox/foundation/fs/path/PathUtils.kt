package dev.psiae.mltoolbox.foundation.fs.path

fun Path.startsWith(
    other: Path,
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

fun Path.endsWith(
    other: Path,
    normalize: Boolean = true
): Boolean {

    if (normalize)
        normalized().endsWith(other.normalized(), false)

    if (this.root != null)
        return this == other

    val shift = this.segments.size - other.segments.size

    if (shift < 0)
        return false
    return this.segments.subList(shift, segments.size) == other.segments
}

val Path.invariantSeparatorsPathString
    get() = toString().replace('\\', '/')

val Path.backslashSeparatorsPathString
    get() = toString().replace('\\', '/')

