package dev.psiae.mltoolbox.foundation.fs.path

import dev.psiae.mltoolbox.foundation.fs.file.JFile
import dev.psiae.mltoolbox.foundation.fs.internal.path.isAbsolute
import dev.psiae.mltoolbox.foundation.fs.internal.path.isRelative
import dev.psiae.mltoolbox.foundation.fs.internal.path.name
import dev.psiae.mltoolbox.foundation.fs.internal.path.nameBytes
import dev.psiae.mltoolbox.foundation.fs.internal.path.parent
import dev.psiae.mltoolbox.foundation.fs.internal.path.relativeToImpl
import dev.psiae.mltoolbox.foundation.fs.internal.path.resolveImpl
import dev.psiae.mltoolbox.foundation.fs.internal.path.root
import dev.psiae.mltoolbox.foundation.fs.internal.path.segments
import dev.psiae.mltoolbox.foundation.fs.internal.path.segmentsBytes
import dev.psiae.mltoolbox.foundation.fs.internal.path.toPath
import dev.psiae.mltoolbox.foundation.fs.internal.path.volumeLetter
import okio.Buffer
import okio.ByteString
import okio.FileSystem
import java.nio.file.Paths
import okio.Path.Companion.toPath as okioToPath

/**
 * A hierarchical address on a file system. A path is an identifier only; a [FileSystem] is required
 * to access the file that a path refers to, if any.
 *
 * Similar to [okio.Path] with the following difference:
 *   1. Explicitly constructing Path with empty string will remain "", but any operation that uses or results an empty
 *      path will be normalized to "."
 *
 */
class Path internal constructor(
    val bytes: ByteString
) : Comparable<Path> {

    /**
     * Whether this is an empty (unset) path
     */
    val isEmpty: Boolean
        get() = bytes == EMPTY.bytes

    val root: Path?
        get() = root()

    val segments: List<String>
        get() = segments()

    val segmentsBytes: List<ByteString>
        get() = segmentsBytes()

    val isAbsolute: Boolean
        get() = isAbsolute()

    val isRelative: Boolean
        get() = isRelative()

    val volumeLetter: Char?
        get() = volumeLetter()

    val parent: Path?
        get() = parent()

    val nameBytes: ByteString
        get() = nameBytes()

    val name: String
        get() = name()

    @JvmName("resolve")
    operator fun div(child: String): Path = resolveImpl(child, normalize = false)
    @JvmName("resolve")
    operator fun div(child: Path): Path = resolveImpl(child, normalize = false)

    fun resolve(child: String, normalize: Boolean = false): Path = resolveImpl(child, normalize = normalize)
    fun resolve(child: Path, normalize: Boolean = false): Path = resolveImpl(child, normalize = normalize)

    fun normalized(): Path = bytes.utf8().toPath(true)

    fun relativeTo(other: Path): Path = relativeToImpl(other)

    fun toJFile(): JFile = JFile(toString())
    fun toJNioPath(): JNioPath = Paths.get(toString())

    override fun compareTo(other: Path): Int {
        return bytes.compareTo(other.bytes)
    }

    override fun equals(other: Any?): Boolean {
        return other is Path && other.bytes == bytes
    }

    override fun hashCode(): Int {
        return bytes.hashCode()
    }

    override fun toString(): String {
        return bytes.utf8()
    }


    companion object {
        val DIRECTORY_SEPARATOR: String = JFile.separator
        val EMPTY = Path(Buffer().writeUtf8("").readByteString())
        val CWD = Path(Buffer().writeUtf8(".").readByteString())

        fun String.toPath(normalize: Boolean = false): Path {
            return if (isEmpty())
                Path(EMPTY.bytes)
            else
                Buffer().writeUtf8(this).toPath(normalize)
        }
        fun JFile.toFsPath(normalize: Boolean = false): Path = toString().toPath(normalize)
        fun JNioPath.toFsPath(normalize: Boolean = false): Path = toString().toPath(normalize)

        fun Path.toOkioPath(): OkioPath = bytes.utf8().okioToPath()

        fun of(uri: java.net.URI): Path {
            val scheme = requireNotNull(uri.scheme) {
                "Missing Scheme"
            }

            if (scheme.equals("file", ignoreCase = true))
                return JNioPath.of(uri).toFsPath()

            throw IllegalArgumentException("Unsupported Scheme: $scheme")
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun Path?.orEmpty(): Path = this ?: EMPTY

        inline fun Path.ifEmpty(defaultValue: () -> Path): Path {
            return if (isEmpty) defaultValue() else this
        }
    }
}