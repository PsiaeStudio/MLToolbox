package dev.psiae.mltoolbox.foundation.fs

import dev.psiae.mltoolbox.foundation.fs.file.JvmFileObject
import dev.psiae.mltoolbox.foundation.fs.file.NioFileObject
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioAccessDeniedException
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioDirectoryNotEmptyException
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioFileAlreadyExistsException
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioFileSystemException
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioNoSuchFileException
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioNotDirectoryException
import dev.psiae.mltoolbox.foundation.fs.path.JNioPath
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toFsPath
import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.util.stream.Collectors
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.isDirectory

class NioSystemFileSystem internal constructor(): JvmSystemFileSystem() {

    override fun file(path: Path): NioFileObject {
        return NioFileObject(path, this)
    }

    override fun file(vararg path: Path): NioFileObject {
        return NioFileObject(
            path.fold(Path.EMPTY) { acc, path -> acc / path },
            this
        )
    }

    override fun file(
        path: Path,
        vararg pathString: String
    ): JvmFileObject {
        return NioFileObject(
            pathString.fold(path) { acc, path -> acc / path },
            this
        )
    }

    override fun isValidPath(path: Path): Boolean {
        try {
            path.toJNioPath()
        } catch (_: InvalidPathException) {
            return false
        }
        return true
    }

    override fun canonicalize(path: Path): Path {
        val realPath = try {
            path.toJNioPath().toRealPath()
        } catch (e: JNioFileSystemException) {
            throw nioFsException(path, null, e)
        }
        return realPath.toFsPath()
    }

    override fun weakCanonicalize(path: Path): Path {
        val nioPath = path.toJNioPath()
        val nioAbsolutePath = nioPath.toAbsolutePath()

        var current: JNioPath? = nioAbsolutePath
        val tails = mutableListOf<JNioPath>()
        while (current != null) {
            try {
                var base = current.toRealPath()
                for (i in tails.indices.reversed()) {
                    val tail = tails[i]
                    base = base.resolve(tail)
                }
                return base.toFsPath(normalize = true)
            } catch (_: JNioNoSuchFileException) {
                current = current.parent
            }
        }

        return nioAbsolutePath.toFsPath()
    }

    override fun list(path: Path): List<Path> = list(path, true)!!

    override fun listOrNull(path: Path): List<Path>? = list(path,false)

    private fun list(path: Path, throwIOExceptions: Boolean): List<Path>? {
        val file = path.toJNioPath()
        val entries = try {
            Files.list(file).collect(Collectors.toList())
        } catch (ioe: IOException) {
            if (throwIOExceptions) {
                if (ioe is JNioFileSystemException) {
                    throw nioFsException(path, null, ioe)
                }
                throw ioe
            }
            if (
                ioe is JNioNoSuchFileException ||
                ioe is JNioNotDirectoryException
            ) return null
            throw ioe
        }
        val paths = entries.mapTo(mutableListOf()) { it.toFsPath() }
        paths.sort()
        return paths
    }


    internal fun nioFsException(
        file: Path,
        otherFile: Path?,
        cause: JNioFileSystemException
    ): FileSystemException {
        return when (cause) {
            is JNioNoSuchFileException -> throw NoSuchFileException(
                file,
                otherFile,
                "no such file",
                cause
            )
            is JNioAccessDeniedException -> throw AccessDeniedException(
                file,
                otherFile,
                "access denied",
                cause
            )
            is JNioFileAlreadyExistsException -> throw FileAlreadyExistsException(
                file,
                otherFile,
                "file already exists",
                cause
            )
            is JNioDirectoryNotEmptyException -> throw DirectoryNotEmptyException(
                file,
                otherFile,
                "directory is not empty",
                cause
            )
            is JNioNotDirectoryException -> throw NotDirectoryException(
                file,
                otherFile,
                "not a directory",
                cause
            )
            else -> FileSystemException(
                file,
                otherFile,
                "file system error",
                cause
            )
        }
    }

    override fun createDirectory(path: Path) {
        val file = file(path)
        val exist = file.exists()
        if (exist)
            throw FileAlreadyExistsException(path, null, "a file already exist at path")

        try {
            path.toJNioPath().createDirectory()
        } catch (e: JNioFileSystemException) {
            throw nioFsException(path, null, e)
        }
    }

    override fun createFile(path: Path) {
        val file = file(path)
        val exist = file.exists()
        if (exist)
            throw FileAlreadyExistsException(path, null, "a file already exist at path")

        try {
            path.toJNioPath().createFile()
        } catch (e: JNioFileSystemException) {
            throw nioFsException(path, null, e)
        }
    }

    companion object {
    }
}