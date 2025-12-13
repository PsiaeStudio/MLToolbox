package dev.psiae.mltoolbox.foundation.fs

import dev.psiae.mltoolbox.foundation.fs.path.Path
import java.io.IOException
import kotlin.text.StringBuilder

private fun constructMessage(file: Path, other: Path?, reason: String?): String {
    val sb = StringBuilder(file.toString())
    if (other != null) {
        sb.append(" -> $other")
    }
    if (reason != null) {
        sb.append(": $reason")
    }
    return sb.toString()
}

/**
 * A base exception class for file system exceptions.
 * @property file the file on which the failed operation was performed.
 * @property other the second file involved in the operation, if any (for example, the target of a copy or move)
 * @property reason the description of the error
 */
open class FileSystemException(
    public val file: Path,
    public val other: Path? = null,
    public val reason: String? = null,
    cause: Throwable? = null
) : IOException(constructMessage(file, other, reason), cause)

/**
 * An exception class which is used when some file to create or copy to already exists.
 */
class FileAlreadyExistsException(
    file: Path,
    other: Path? = null,
    reason: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, other, reason, cause)

/**
 * An exception class which is used when we have not enough access for some operation.
 */
class AccessDeniedException(
    file: Path,
    other: Path? = null,
    reason: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, other, reason, cause)

/**
 * An exception class which is used when file to copy does not exist.
 */
class NoSuchFileException(
    file: Path,
    other: Path? = null,
    reason: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, other, reason, cause)

/**
 * An exception class which is used when attempting to delete directory but is not empty
 */
class DirectoryNotEmptyException(
    file: Path,
    other: Path? = null,
    reason: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, other, reason, cause)

/**
 * An exception class which is used when attempting to list a file that is not a directory
 */
class NotDirectoryException(
    file: Path,
    other: Path? = null,
    reason: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, other, reason, cause)