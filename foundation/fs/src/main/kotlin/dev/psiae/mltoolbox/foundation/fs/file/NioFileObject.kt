package dev.psiae.mltoolbox.foundation.fs.file

import com.sun.nio.file.ExtendedCopyOption
import com.sun.nio.file.ExtendedOpenOption
import dev.psiae.mltoolbox.core.catchOrRethrow
import dev.psiae.mltoolbox.foundation.fs.FileSystemException
import dev.psiae.mltoolbox.foundation.fs.NioSystemFileSystem
import dev.psiae.mltoolbox.foundation.fs.NoSuchFileException
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioFileSystemException
import dev.psiae.mltoolbox.foundation.fs.jnio.JNioNoSuchFileException
import dev.psiae.mltoolbox.foundation.fs.native.windows.WindowsNativeFileAttributes
import dev.psiae.mltoolbox.foundation.fs.native.windows.WindowsPath
import dev.psiae.mltoolbox.foundation.fs.path.JNioPath
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toFsPath
import org.w3c.dom.Attr
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.FileTime

class NioFileObject internal constructor(
    path: Path,
    override val fs: NioSystemFileSystem
) : JvmFileObject(path, fs) {

    private val jNioPath by lazy(LazyThreadSafetyMode.NONE) { path.toJNioPath() }

    override fun metadataOrNull(
        followLinks: Boolean
    ): FileMetadata? {
        val attributes = try {
            Files.readAttributes(
                jNioPath,
                BasicFileAttributes::class.java,
                *(if (!followLinks) arrayOf(LinkOption.NOFOLLOW_LINKS) else emptyArray())
            )
        } catch (_: JNioNoSuchFileException) {
            return null
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)

            /*
            note: figure out why is this catch
            return null
            */
        }

        val symlinkTarget: JNioPath? = try {
            if (attributes.isSymbolicLink) {
                Files.readSymbolicLink(jNioPath)
            } else {
                null
            }
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }

        return FileMetadata(
            isRegularFile = attributes.isRegularFile,
            isDirectory = attributes.isDirectory,
            symlinkTarget = symlinkTarget?.toFsPath(),
            size = attributes.size(),
            createdAtMillis = attributes.creationTime()?.zeroToNull(),
            lastModifiedAtMillis = attributes.lastModifiedTime()?.zeroToNull(),
            lastAccessedAtMillis = attributes.lastAccessTime()?.zeroToNull(),
        )
    }

    override fun metadata(followLinks: Boolean): FileMetadata {
        return metadataOrNull(followLinks)
            ?: throw NoSuchFileException(path, null, null)
    }



    private fun FileTime.zeroToNull(): Long? {
        return toMillis().takeIf { it != 0L }
    }

    override fun delete(followLinks: Boolean) {
        try {
            val targetPath = if (followLinks) jNioPath.toRealPath() else jNioPath
            Files.delete(targetPath)
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
    }

    override fun openReadOnly(followLinks: Boolean): FileHandle {
        val channel = try {
            val options = if (followLinks) {
                arrayOf(StandardOpenOption.READ)
            } else {
                arrayOf<OpenOption>(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)
            }
            FileChannel.open(
                jNioPath,
                *options
            )
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
        return NioFileHandle(readWrite = false, fileChannel = channel)
    }

    override fun isReadable(): Boolean {
        return try {
            Files.isReadable(jNioPath)
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
    }

    override fun isWritable(): Boolean {
        return try {
            Files.isWritable(jNioPath)
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
    }

    override fun isExecutable(): Boolean {
        return try {
            Files.isExecutable(jNioPath)
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
    }

    override fun isReadOnly(followLinks: Boolean): Boolean {
        return try {
            val attributesView = dosAttributesView(jNioPath, followLinks)
            val attributes = attributesView.readAttributes()
            attributes.isReadOnly
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
    }

    override fun setReadOnly(readOnly: Boolean, followLinks: Boolean) {
        try {
            val attributesView = dosAttributesView(jNioPath, followLinks)
            attributesView.setReadOnly(readOnly)
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
    }

    override fun tryOpenForDelete(followLinks: Boolean): Boolean {
        runCatching {
            if (!tryChangeAttributeForDelete())
                return false

            val attributesView = dosAttributesView(jNioPath, followLinks)
            val attributes = attributesView.readAttributes()

            FileChannel.open(
                jNioPath,
                *run {
                    arrayOf<OpenOption>(
                        StandardOpenOption.READ,
                        ExtendedOpenOption.NOSHARE_READ,
                        ExtendedOpenOption.NOSHARE_WRITE,
                        ExtendedOpenOption.NOSHARE_DELETE
                    ) + if (!followLinks) arrayOf(LinkOption.NOFOLLOW_LINKS) else emptyArray()
                }
            ).use { fc ->
                /*fc.tryLock(0, Long.MAX_VALUE, attributes.isReadOnly)
                    ?.use {  }
                    ?: throw IOException("Locked by another program")*/
            }
        }.catchOrRethrow { e ->
            when (e) {
                is IOException -> return false
            }
        }
        return true
    }

    override fun tryChangeAttributeForDelete(): Boolean {
        runCatching {
            WindowsNativeFileAttributes.checkCanWriteAttributes(WindowsPath(fs, path))
        }.catchOrRethrow { e ->
            when (e) {
                is IOException -> return false
            }
        }
        return true
    }

    override fun isSameFileAs(otherPath: Path): Boolean {
        return try {
            Files.isSameFile(jNioPath, otherPath.toJNioPath())
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, otherPath, e)
        }
    }

    override fun copyTo(target: Path, overwrite: Boolean, followLinks: Boolean) {
        try {
            val options = mutableSetOf<CopyOption>(ExtendedCopyOption.INTERRUPTIBLE)
            if (overwrite)
                options.add(StandardCopyOption.REPLACE_EXISTING)
            if (!followLinks)
                options.add(LinkOption.NOFOLLOW_LINKS)
            Files.copy(jNioPath, target.toJNioPath(), *options.toTypedArray())
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, target, e)
        }
    }

    override fun  followLinks(): FileObject {
        return try {
            NioFileObject(
                if (metadata().symlinkTarget != null)
                    jNioPath.toRealPath().toFsPath()
                else
                    path,
                fs = fs
            )
        } catch (e: JNioFileSystemException) {
            throw fs.nioFsException(path, null, e)
        }
    }



    companion object {

        private fun dosAttributesView(
            path: JNioPath,
            followLinks: Boolean,
            otherPath: Path? = null
        ): java.nio.file.attribute.DosFileAttributeView {
            return Files.getFileAttributeView(
                path,
                DosFileAttributeView::class.java,
                *run {
                    if (followLinks) {
                        emptyArray()
                    } else {
                        arrayOf(LinkOption.NOFOLLOW_LINKS)
                    }
                }
            ) ?: attributesViewNotAvailableError(
                path.toFsPath(),
                otherPath,
                "dos"
            )
        }

        private fun attributesViewNotAvailableError(
            path: Path,
            otherPath: Path? = null,
            name: String
        ): Nothing {
            throw FileSystemException(
                path,
                otherPath,
                "attributes view '$name' is not available"
            )
        }
    }
}