package dev.psiae.mltoolbox.foundation.fs.file

import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.foundation.fs.AccessDeniedException
import dev.psiae.mltoolbox.foundation.fs.FileAlreadyExistsException
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.fs.FileSystemException
import dev.psiae.mltoolbox.foundation.fs.NoSuchFileException
import dev.psiae.mltoolbox.foundation.fs.internal.IllegalFileNameException
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.startsWith
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InterruptedIOException
import java.util.Objects

/**
 * A Virtual File on a File System
 *
 * This is used for file operations, for path operations use [FileSystem] instead
 *
 * note: consider naming this to `VFile` or `VirtualFile`
 */
abstract class FileObject internal constructor(
    val path: Path,
    open val fs: FileSystem,
) {
    /**
     * Returns metadata of the file, or null if file does not exist.
     *
     * legacy: may be null if the file is inaccessible
     *
     * @throws FileSystemException if file is inaccessible.
     * @throws IOException an I/O error occurs.
     */
    @Throws(IOException::class)
    abstract fun metadataOrNull(
        followLinks: Boolean = false
    ): FileMetadata?

    /**
     * Returns metadata of the file.
     *
     * @throws FileSystemException if file does not exist or inaccessible.
     * @throws FileNotFoundException (legacy) if file does not exist or inaccessible.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    abstract fun metadata(
        followLinks: Boolean = false
    ): FileMetadata

    /**
     * Returns true if the file does exist.
     *
     * legacy: may be false if inaccessible
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun exists(
        followLinks: Boolean = false,
    ): Boolean = metadataOrNull(followLinks) != null

    /**
     * Returns true if the file does not exist.
     *
     * legacy: may be true if inaccessible
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun notExists(
        followLinks: Boolean = false
    ): Boolean = metadataOrNull(followLinks) == null

    /**
     * Returns true if the file is a directory
     *
     * legacy: may be false if inaccessible
     *
     * @throws FileSystemException if file is inaccessible.
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun isDirectory(
        followLinks: Boolean = false
    ): Boolean = metadataOrNull(followLinks)?.isDirectory == true

    /**
     * Returns true if the file is a regular file
     *
     * legacy: may be false if inaccessible
     *
     * @throws FileSystemException if file is inaccessible.
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun isRegularFile(
        followLinks: Boolean = false
    ): Boolean = metadataOrNull(followLinks)?.isRegularFile == true

    /**
     * Deletes the file.
     *
     * If the file is a directory then the directory must be empty.
     *
     * Fail if the file is read-only, see [deleteRecursively]
     *
     * @throws FileSystemException if the file does not exist or inaccessible.
     * @throws FileNotFoundException (legacy) if the file does not exist or inaccessible.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    abstract fun delete(
        followLinks: Boolean = false
    )

    /**
     * Deletes the file if it exists.
     *
     * legacy: may be false if inaccessible
     *
     * If the file is a directory then the directory must be empty.
     *
     * @return true if the file was deleted; false if the file did not exist.
     *
     * @throws FileSystemException if the file is inaccessible
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun deleteIfExist(
        followLinks: Boolean = false
    ): Boolean = try {
        delete(followLinks)
        true
    } catch (fnf: FileNotFoundException) {
        false
    } catch (nsf: NoSuchFileException) {
        false
    }

    @Throws(IOException::class)
    abstract fun isReadOnly(
        followLinks: Boolean = false
    ): Boolean

    @Throws(IOException::class)
    abstract fun setReadOnly(
        readOnly: Boolean,
        followLinks: Boolean = false
    )

    /**
     * Recursively delete the file at [path]
     *
     * Does not follow link.
     *
     * If a file is read-only then said attribute is removed before deletion
     */
    @Throws(IOException::class)
    open fun deleteRecursively() {
        runCatching {
            if (isDirectory()) {
                val sequence = sequence {
                    collectRecursively(
                        fileSystem = fs,
                        stack = ArrayDeque(),
                        path = path,
                        followSymlinks = false,
                        postorder = true,
                    )
                }
                val iterator = sequence.iterator()
                loop@ while (iterator.hasNext()) {
                    val pathToDelete = iterator.next()
                    val fileToDelete = fs.file(pathToDelete)

                    if (Thread.interrupted())
                        throw InterruptedIOException("interrupted")

                    runCatching {
                        val fileToDeleteMetadata = fileToDelete.metadataOrNull()
                            ?: continue@loop
                        if (!fileToDeleteMetadata.isDirectory)
                            if (fileToDelete.isReadOnly())
                                fileToDelete.setReadOnly(false)
                    }.catchOrRethrow { e ->
                        if (e is IOException) {
                            try {
                                if (!fileToDelete.exists())
                                    continue
                            } catch (_: IOException) {}
                        }
                    }

                    fileToDelete.deleteIfExist()
                }
            } else {
                if (isReadOnly())
                    setReadOnly(false)
                deleteIfExist()
            }
        }.catchOrRethrow { e ->
            if (e is IOException) {
                try {
                    if (!exists())
                        return
                } catch (_: IOException) {}
            }
        }
    }

    @Throws(IOException::class)
    abstract fun isReadable(): Boolean

    @Throws(IOException::class)
    abstract fun isWritable(): Boolean

    @Throws(IOException::class)
    abstract fun isExecutable(): Boolean

    @Throws(IOException::class)
    fun isSymbolicLink(): Boolean = metadata().symlinkTarget != null

    @Throws(IOException::class)
    abstract fun tryOpenForDelete(followLinks: Boolean = false): Boolean

    @Throws(IOException::class)
    abstract fun tryChangeAttributeForDelete(): Boolean

    /**
     * Test whether [deleteRecursively] should succeed
     *
     * Returns `false` if a file within [path] cannot be deleted.
     * Returns `true` if [path] does not exist.
     */
    @Throws(IOException::class)
    fun canDeleteRecursively(): Boolean {
        runCatching {
            val canonicalizedPath = try {
                fs.canonicalize(path)
            } catch (_: NoSuchFileException) {
                return true
            }

            canonicalizedPath.parent?.let { parent ->
                if (!fs.file(parent).isWritable())
                    return !fs.file(parent).exists()
            }

            val metadata = metadataOrNull()
                ?: return true

            if (metadata.isDirectory) {

                if (!isExecutable())
                    return !exists()

                val sequence = sequence {
                    collectRecursively(
                        fileSystem = fs,
                        stack = ArrayDeque(),
                        path = path,
                        followSymlinks = false,
                        postorder = false,
                    )
                }
                val iterator = sequence.iterator()
                loop@ while (iterator.hasNext()) {
                    val pathToDelete = iterator.next()
                    val fileToDelete = fs.file(pathToDelete)

                    if (Thread.interrupted())
                        throw InterruptedIOException("interrupted")

                    runCatching {
                        val fileToDeleteMetadata = fileToDelete.metadataOrNull()
                            ?: continue@loop

                        if (fileToDeleteMetadata.isDirectory) {
                            if (!fileToDelete.isWritable())
                                if (fileToDelete.exists())
                                    return false
                        } else if (fileToDeleteMetadata.isRegularFile) {
                            if (!fileToDelete.tryOpenForDelete())
                                if (fileToDelete.exists())
                                    return false
                        } else if (fileToDelete.isReadOnly()) {
                            if (!fileToDelete.tryChangeAttributeForDelete())
                                if (fileToDelete.exists())
                                    return false
                        }
                    }.catchOrRethrow { e ->
                        if (e is IOException) {
                            try {
                                if (!fileToDelete.exists())
                                    continue@loop
                            } catch (_: IOException) {}
                        }
                    }
                }
            } else if (metadata.isRegularFile) {
                if (!tryOpenForDelete())
                    return !exists()
            } else if (isReadOnly()) {
                if (!tryChangeAttributeForDelete())
                    return !exists()
            }
        }.catchOrRethrow { e ->
            if (e is IOException) {
                if (e is InterruptedIOException)
                    throw e
                try {
                    if (!exists())
                        return true
                } catch (_: IOException) {}
            }
        }
        return true
    }

    @Throws(IOException::class)
    abstract fun openReadOnly(followLinks: Boolean = true): FileHandle

    @Throws(IOException::class)
    protected fun checkExists(
        theOtherPath: Path,
        followLinks: Boolean = true
    ) {
        metadataOrNull(followLinks)
            ?: throw NoSuchFileException(path, theOtherPath)
    }

    @Throws(IOException::class)
    abstract fun isSameFileAs(otherPath: Path): Boolean


    @Throws(IOException::class)
    abstract fun copyTo(target: Path, overwrite: Boolean = false, followLinks: Boolean = true)

    @Throws(IOException::class)
    fun copyToRecursively(
        targetPath: Path,
        followLinks: Boolean = false,
        overwrite: Boolean = false,
    ) {
        checkExists(targetPath, followLinks)
        val targetFile = fs.file(targetPath)

        if (targetFile.exists() && isSameFileAs(targetPath))
            // copy to itself is no-op
            return

        if (followLinks || !isSymbolicLink()) {
            val targetExistAndNotSymlink = targetFile.exists() && !targetFile.isSymbolicLink()
            if (targetExistAndNotSymlink && isSameFileAs(targetPath)) {
                // KT-38678
                // source and target files are the same entry
                return
            } else {
                val isSubDirectory = when {
                    targetExistAndNotSymlink -> fs.canonicalize(targetPath).startsWith(fs.canonicalize(this.path))
                    else -> targetPath.parent
                        ?.let { fs.exists(it) && fs.canonicalize(it).startsWith(fs.canonicalize(this.path)) }
                        ?: false
                }
                if (isSubDirectory) {
                    throw FileSystemException(
                        path,
                        targetPath,
                        "Recursively copying a directory into its subdirectory is prohibited."
                    )
                }
            }
        }

        val normalizedTargetPath = targetPath.normalized()

        val sequence = sequence {
            collectRecursively(
                fileSystem = fs,
                stack = ArrayDeque(),
                path = path,
                followSymlinks = false,
                postorder = false,
            )
        }
        val iterator = sequence.iterator()
        while (iterator.hasNext()) {
            val sourcePath = iterator.next()
            val sourceFile = fs.file(sourcePath)

            if (Thread.interrupted())
                throw InterruptedIOException("interrupted")

            val relativePath = sourcePath.relativeTo(path)
            val destPath = targetPath.resolve(relativePath)

            val destFile = fs.file(destPath)
            if (!destPath.normalized().startsWith(normalizedTargetPath))
                throw IllegalFileNameException(
                    sourcePath,
                    destPath,
                    "Copying files to outside the specified target directory is prohibited. The directory being recursively copied might contain an entry with an illegal name."
                )

            val sourceAttrs = sourceFile.metadataOrNull(followLinks = false)
                ?: continue

            if (sourceAttrs.isDirectory) {
                if (!fs.exists(destPath)) {
                    fs.createDirectory(destPath)
                } else {
                    val destAttrs = destFile.metadata(followLinks = false)
                    if (!destAttrs.isDirectory) {
                        if (overwrite) {
                            destFile.deleteIfExist()
                            fs.createDirectory(destPath)
                        } else {
                            throw FileAlreadyExistsException(
                                sourcePath,
                                destPath,
                                "Destination already exists and is not a Directory"
                            )
                        }
                    }
                }
            } else {
                if (!fs.exists(destPath)) {
                    sourceFile.copyTo(destPath, overwrite)
                } else {
                    if (overwrite) {
                        val destAttrs = destFile.metadataOrNull()
                        if (destAttrs?.isDirectory == true) {
                            destFile.deleteRecursively()
                        } else {
                            destFile.deleteIfExist()
                        }
                        sourceFile.copyTo(destPath, overwrite = true)
                    } else {
                        throw FileAlreadyExistsException(
                            sourcePath,
                            destPath,
                            "Destination file already exists"
                        )
                    }
                }
            }
        }
    }

    /**
     * Follow all links if this file have a symlink target
     *
     * If any link is followed, then the path becomes absolute
     *
     * @throws FileSystemException If real path is inaccessible
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    abstract fun followLinks(): FileObject

    override fun equals(other: Any?): Boolean {
        return other is FileObject && other.path == path && other.fs == fs
    }

    override fun hashCode(): Int {
        return Objects.hash(path, fs)
    }

    override fun toString(): String {
        return "FileObject: $path"
    }
}

@Throws(IOException::class)
internal suspend fun SequenceScope<Path>.collectRecursively(
    fileSystem: FileSystem,
    stack: ArrayDeque<Path>,
    path: Path,
    followSymlinks: Boolean,
    postorder: Boolean,
) {
    // For listRecursively, visit enclosing directory first.
    if (!postorder) {
        yield(path)
    }

    val children = fileSystem.listOrNull(path) ?: emptyList()
    if (children.isNotEmpty()) {
        // Figure out if path is a symlink and detect symlink cycles.
        var symlinkPath = path
        var symlinkCount = 0
        while (true) {
            if (followSymlinks && symlinkPath in stack) throw IOException("symlink cycle at $path")
            symlinkPath = fileSystem.symlinkTarget(symlinkPath) ?: break
            symlinkCount++
        }

        // Recursively visit children.
        if (followSymlinks || symlinkCount == 0) {
            stack.addLast(symlinkPath)
            try {
                for (child in children) {
                    collectRecursively(fileSystem, stack, child, followSymlinks, postorder)
                }
            } finally {
                stack.removeLast()
            }
        }
    }

    // For deleteRecursively, visit enclosing directory last.
    if (postorder) {
        yield(path)
    }
}

@Throws(IOException::class)
private fun FileSystem.symlinkTarget(path: Path): Path? {
    val target = path.file().metadata().symlinkTarget ?: return null
    return path.parent!!.div(target)
}