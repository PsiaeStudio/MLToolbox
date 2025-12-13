package dev.psiae.mltoolbox.foundation.fs

import dev.psiae.mltoolbox.core.JvmProperties
import dev.psiae.mltoolbox.foundation.fs.file.JFile
import dev.psiae.mltoolbox.foundation.fs.file.JvmFileObject
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toFsPath
import dev.psiae.mltoolbox.foundation.fs.path.Path.Companion.toPath
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Note: we don't use this class
 */
abstract class JvmSystemFileSystem protected constructor() : FileSystem() {

    override val defaultDirectory: Path = JvmProperties.USER_DIR.toPath().also {
        check(it.isAbsolute) { "USER_DIR is not an absolute path: $it" }
    }

    override fun file(path: Path): JvmFileObject {
        return JvmFileObject(path, this)
    }

    override fun file(vararg path: Path): JvmFileObject {
        return JvmFileObject(
            path.fold(Path.EMPTY) { acc, path -> acc / path },
            this
        )
    }

    override fun file(
        path: Path,
        vararg pathString: String
    ): JvmFileObject {
        return JvmFileObject(
            pathString.fold(path) { acc, pathStr -> acc / pathStr },
            this
        )
    }

    override fun canonicalize(path: Path): Path {
        val canonicalFile = path.toJFile().canonicalFile
        if (!canonicalFile.exists()) throw FileNotFoundException("no such file")
        return canonicalFile.toFsPath()
    }

    override fun weakCanonicalize(path: Path): Path {
        val file =
            if (path.isAbsolute)
                path.toJFile()
            else
                JFile(defaultDirectory.toJFile().canonicalFile, path.toString())

        var current: JFile? = file
        val tails = mutableListOf<String>()

        while (current != null) {
            if (current.exists()) {
                var base = current.canonicalFile
                for (i in tails.indices.reversed()) {
                    val tail = tails[i]
                    base = JFile(base, tail)
                }
                return base.absoluteFile.toFsPath(normalize = true)
            }
            val name = current.name
            if (name.isNotEmpty())
                tails.add(current.name)
            current = current.parentFile
        }
        return file.absoluteFile.toFsPath(normalize = true)
    }

    override fun list(path: Path): List<Path> = list(path, true)!!

    override fun listOrNull(path: Path): List<Path>? = list(path,false)

    private fun list(path: Path, throwOnFailure: Boolean): List<Path>? {
        val file = path.toJFile()
        val entries = file.list()
            ?: if (throwOnFailure) {
                if (!file.exists()) throw FileNotFoundException("no such file: $file")
                throw IOException("failed to list $file")
            } else {
                return null
            }
        val paths = entries.mapTo(mutableListOf()) { path / it }
        paths.sort()
        return paths
    }
}