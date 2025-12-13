package dev.psiae.mltoolbox.foundation.fs.file

import dev.psiae.mltoolbox.foundation.fs.JvmSystemFileSystem
import dev.psiae.mltoolbox.foundation.fs.path.Path

open class JvmFileObject internal constructor(
    path: Path,
    override val fs: JvmSystemFileSystem
) : FileObject(path, fs) {

    private val jFile by lazy(LazyThreadSafetyMode.NONE) { path.toJFile() }

    override fun metadataOrNull(followLinks: Boolean): FileMetadata? {
        TODO("Not yet implemented")
    }

    override fun metadata(followLinks: Boolean): FileMetadata {
        TODO("Not yet implemented")
    }

    override fun delete(followLinks: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isReadOnly(followLinks: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun setReadOnly(readOnly: Boolean, followLinks: Boolean) {
        TODO("Not yet implemented")
    }

    override fun deleteRecursively() {
        super.deleteRecursively()
    }

    override fun isReadable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isWritable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isExecutable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun tryOpenForDelete(followLinks: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun tryChangeAttributeForDelete(): Boolean {
        TODO("Not yet implemented")
    }

    override fun openReadOnly(followLinks: Boolean): FileHandle {
        TODO("Not yet implemented")
    }

    override fun isSameFileAs(otherPath: Path): Boolean {
        TODO("Not yet implemented")
    }

    override fun copyTo(
        target: Path,
        overwrite: Boolean,
        followLinks: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun followLinks(): FileObject {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }
}