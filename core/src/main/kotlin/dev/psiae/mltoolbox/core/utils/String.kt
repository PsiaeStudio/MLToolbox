package dev.psiae.mltoolbox.core.utils

import java.lang.StringBuilder

fun String.requireStartsWith(
    prefix: String,
    ignoreCase: Boolean = false,
    lazyMsg: () -> String
): Unit {
    require(startsWith(prefix, ignoreCase), lazyMsg)
}

fun String.removePrefix(
    prefix: String,
    ignoreCase: Boolean = false
) = if (startsWith(prefix, ignoreCase = ignoreCase)) drop(prefix.length) else this

fun String.removeSuffix(
    suffix: String,
    ignoreCase: Boolean = false
) = if (endsWith(suffix, ignoreCase = ignoreCase)) dropLast(suffix.length) else this

fun String.ensureSuffix(
    suffix: Char,
    ignoreCase: Boolean = false
) = if (!endsWith(suffix, ignoreCase = ignoreCase)) plus(suffix) else this

fun String.ensureSuffix(
    suffix: String,
    ignoreCase: Boolean = false
) = if (!endsWith(suffix, ignoreCase = ignoreCase)) plus(suffix) else this

fun String.ensurePrefix(
    suffix: Char,
    ignoreCase: Boolean = false
) = if (!startsWith(suffix, ignoreCase = ignoreCase)) suffix.plus(this) else this

fun String.ensurePrefix(
    suffix: String,
    ignoreCase: Boolean = false
) = if (!startsWith(suffix, ignoreCase = ignoreCase)) suffix.plus(this) else this

fun String.endsWithLineSeparator(): Boolean {
    // empty
    if (isEmpty())
        return false
    // Unix and Windows
    if (endsWith("\n"))
        return true
    // Mac
    if (endsWith("\r"))
        return true
    return false
}

fun String.uppercaseFirstChar(): String =
    transformFirstCharIfNeeded(
        shouldTransform = { it.isLowerCase() },
        transform = { it.uppercaseChar() }
    )

fun String.lowercaseFirstChar(): String =
    transformFirstCharIfNeeded(
        shouldTransform = { it.isUpperCase() },
        transform = { it.lowercaseChar() }
    )

private inline fun String.transformFirstCharIfNeeded(
    shouldTransform: (Char) -> Boolean,
    transform: (Char) -> Char
): String {
    if (isNotEmpty()) {
        val firstChar = this[0]
        if (shouldTransform(firstChar)) {
            val sb = StringBuilder(length)
            sb.append(transform(firstChar))
            sb.append(this, 1, length)
            return sb.toString()
        }
    }
    return this
}

fun String?.orNullString() = toString()

fun String.endsWith(
    suffixes: List<String>,
    ignoreCase: Boolean
): Boolean = suffixes.any { endsWith(it, ignoreCase) }