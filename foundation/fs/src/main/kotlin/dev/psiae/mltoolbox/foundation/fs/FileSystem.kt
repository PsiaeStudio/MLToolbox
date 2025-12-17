package dev.psiae.mltoolbox.foundation.fs

import dev.psiae.mltoolbox.foundation.fs.file.FileObject
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toPath
import java.io.IOException
import java.io.FileNotFoundException

/**
 * Local File System
 *
 * similar to [okio.FileSystem] with extended capability
 */
abstract class FileSystem {

    open val defaultDirectory: Path = "/".toPath()

    abstract fun file(path: Path): FileObject
    abstract fun file(vararg path: Path): FileObject
    abstract fun file(path: Path, vararg pathString: String): FileObject

    abstract fun isValidPath(path: Path): Boolean

    /**
     * Returns the canonicalized path of an existing file.
     *
     * Returned path is always an absolute path, symlinks are also resolved.
     *
     * @throws FileSystemException if [path] cannot be resolved because a file does not exist or inaccessible
     * @throws FileNotFoundException if [path] cannot be resolved because a file does not exist or inaccessible (legacy)
     * @throws IOException if an I/O error occurs.
     */
    @Throws(
        IOException::class
    )
    abstract fun canonicalize(
        path: Path
    ): Path

    /**
     * Returns the canonicalized path of a file.
     *
     * Returned path is always an absolute path, symlinks are also resolved.
     *
     * Resolves links on leading segments that `exist`, then appends the tailing segments.
     *  legacy: `exist` may be false due to access denied
     *
     * @throws FileSystemException if [path] cannot be resolved because a file is inaccessible
     * @throws IOException if an I/O error occurs.
     */
    @Throws(
        IOException::class
    )
    abstract fun weakCanonicalize(
        path: Path
    ): Path

    /**
     * Returns the absolute [Path] of [path].
     *
     * If [path] is already absolute then [path] is simply returned.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(
        IOException::class
    )
    fun absolute(path: Path): Path = when {
        path.isAbsolute -> path
        else -> defaultDirectory / path
    }

    /**
     * Returns the files at given [path].
     *
     * The returned list is sorted using natural ordering. If [path] is a relative path, the returned elements will also
     * be relative paths. If it is an absolute path, the returned elements will also be absolute paths.
     *
     * @throws FileSystemException if [path] does not exist or cannot be listed
     * @throws FileNotFoundException if [path] does not exist or inaccessible (legacy)
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    abstract fun list(path: Path): List<Path>

    /**
     * Returns the files at given [path].
     *
     * The returned list is sorted using natural ordering. If [path] is a relative path, the returned elements will also
     * be relative paths. If it is an absolute path, the returned elements will also be absolute paths.
     *
     * Returns null if [path] does not exist or is not a directory
     */
    abstract fun listOrNull(path: Path): List<Path>?

    @Throws(IOException::class)
    fun exists(path: Path): Boolean = file(path).exists()

    @Throws(IOException::class)
    abstract fun createDirectory(path: Path)

    @Throws(IOException::class)
    fun createDirectories(path: Path) {
        val directories = ArrayDeque<Path>()
        var head: Path? = path
        while (head != null && !exists(head)) {
            directories.addFirst(head)
            head = head.parent
        }

        for (path in directories) {
            try {
                createDirectory(path)
            } catch (e: IOException) {
                if (e is FileAlreadyExistsException) {
                    if (!file(path).isDirectory(followLinks = true))
                        throw e
                }
            }
        }
    }

    @Throws(IOException::class)
    fun createDirectoryIfNotExist(path: Path) {
        try {
            createDirectory(path)
        } catch (e: IOException) {
            try {
                if (file(path).exists())
                    return
            } catch (e: IOException) {}
            throw e
        }
    }

    @Throws(IOException::class)
    abstract fun createFile(path: Path)

    @Throws(IOException::class)
    @JvmName("pathFile")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.file(): FileObject = file(this)

    @Throws(IOException::class)
    @JvmName("pathCanonicalize")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.canonicalize(): Path = canonicalize(this)

    @Throws(IOException::class)
    @JvmName("pathAbsolute")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.absolute(): Path = absolute(this)

    @Throws(IOException::class)
    @JvmName("pathList")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.list(): List<Path> = list(this)

    @Throws(IOException::class)
    @JvmName("pathListOrNull")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.listOrNull(): List<Path>? = listOrNull(this)

    @Throws(IOException::class)
    @JvmName("pathExists")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.exists(): Boolean = exists(this)

    @Throws(IOException::class)
    @JvmName("pathCreateDirectory")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.createDirectory(): Unit = createDirectory(this)

    @Throws(IOException::class)
    @JvmName("pathCreateDirectories")
    @Suppress("NOTHING_TO_INLINE")
    inline fun Path.createDirectories(): Unit = createDirectories(this)


    companion object {
        val SYSTEM: FileSystem = NioSystemFileSystem()
    }
}