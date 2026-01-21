package dev.psiae.mltoolbox.foundation.fs.native.windows

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.WString
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.W32APIOptions
import dev.psiae.mltoolbox.foundation.fs.FileSystem
import dev.psiae.mltoolbox.foundation.fs.internal.path.root
import dev.psiae.mltoolbox.foundation.fs.path.Path
import java.nio.file.InvalidPathException

class WindowsPath(
    val fs: FileSystem,
    val path: Path,
    private val isPathNormalized: Boolean = false,
) {
    private var _pathForWin32Calls: String? = null

    private var _isPathTypeAbsolute: Boolean? = null
    private var _isPathTypeUnc: Boolean? = null
    private var _isPathTypeRelative: Boolean? = null
    private var _isPathTypeDriveRelative: Boolean? = null
    private var _isPathTypeDirectoryRelative: Boolean? = null

    private var _root: String? = null
    private var _normalized: String? = null

    private var _parsed = false


    private fun parse() {
        var root = ""

        _isPathTypeAbsolute = false
        _isPathTypeUnc = false
        _isPathTypeDriveRelative = false
        _isPathTypeDirectoryRelative = false

        val pathString = path.toString()
        var input = pathString

        var isExpectedTypeUnc = false
        var isExpectedTypeAbsolute = false
        if (input.startsWith("\\\\?\\")) {
            if (input.startsWith("UNC\\", 4)) {
                isExpectedTypeUnc = true
                input = "\\\\" + input.substring(8)
            } else {
                isExpectedTypeAbsolute = true
                input = input.substring(4)
            }
        }

        val len = input.length
        var off = 0
        if (len > 1) {
            val c0 = input[0]
            val c1 = input[1]
            var next = 2
            if (isSlash(c0) && isSlash(c1)) {
                off = nextNonSlash(input, next, len)
                next = nextSlash(input, off, len)
                if (off == next) throw InvalidPathException(input, "UNC path is missing hostname")
                val host = input.substring(off, next) //host
                off = nextNonSlash(input, next, len)
                next = nextSlash(input, off, len)
                if (off == next) throw InvalidPathException(input, "UNC path is missing sharename")
                root = "\\\\" + host + "\\" + input.substring(off, next) + "\\"
                off = next
            } else if (isLetter(c0) && c1 == ':') {

                if (len > 2 && isSlash(input[2])) {
                    val c2: Char = input[2]
                    // avoid concatenation when root is "D:\"
                    root = if (c2 == '\\') {
                        input.take(3)
                    } else {
                        input.take(2) + '\\'
                    }
                    off = 3
                    _isPathTypeAbsolute = true
                } else {
                    root = input.take(2)
                    off = 2
                    _isPathTypeDriveRelative = true
                }
            }
        }
        if (off == 0) {
            if (len > 0 && isSlash(input[0])) {
                _isPathTypeDirectoryRelative = true
                root = "\\"
            } else {
                _isPathTypeRelative = true
            }
        }

        if (isExpectedTypeAbsolute && _isPathTypeAbsolute != true) {
            // long path prefix
            throw InvalidPathException(input, "Long path prefix can only be used with an absolute path")
        }
        if (isExpectedTypeUnc && _isPathTypeUnc != true) {
            // long UNC path prefix
            throw InvalidPathException(input, "Long UNC path prefix can only be used with a UNC path")
        }

        if (!isPathNormalized) {
            val sb = StringBuilder(input.length)
            sb.append(root)
            _normalized = normalize(sb, input, off)
        } else {
            _normalized = input
        }

        _parsed = true
    }

    private fun normalize(sb: StringBuilder, path: String, off: Int): String {
        var off = off
        val len = path.length
        off = nextNonSlash(path, off, len)
        var start = off
        var lastC = 0.toChar()
        while (off < len) {
            val c = path[off]
            if (isSlash(c)) {
                if (lastC == ' ') throw InvalidPathException(
                    path,
                    "Trailing char <$lastC>",
                    off - 1
                )
                sb.append(path, start, off)
                off = nextNonSlash(path, off, len)
                if (off != len)  //no slash at the end of normalized path
                    sb.append('\\')
                start = off
            } else {
                if (isInvalidPathChar(c)) throw InvalidPathException(
                    path,
                    "Illegal char <$c>",
                    off
                )
                lastC = c
                off++
            }
        }
        if (start != off) {
            if (lastC == ' ') throw InvalidPathException(
                path,
                "Trailing char <$lastC>",
                off - 1
            )
            sb.append(path, start, off)
        }
        return sb.toString()
    }

    private fun isAbsolute(): Boolean {
        return isPathTypeAbsolute || isPathTypeUnc
    }

    private fun getFullPathName0(path: String): String {
        val buf = CharArray(WIN32_MAX_PATH)

        var len: Int = kernel32.GetFullPathNameW(path, buf.size, buf, null)

        if (len > 0) {
            if (len < buf.size) {
                return String(buf, 0, len)
            } else {

                // Microsoft doc says the length includes the null terminator
                // https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-getfullpathnamew#return-value

                // but OpenJDK implementation says it does not?
                // https://github.com/openjdk/jdk/blob/4fd7595f1b607588d9854471a701c2992c6bec60/src/java.base/windows/native/libnio/fs/WindowsNativeDispatcher.c#L1115

                len += 1

                val lpBuf = CharArray(len)

                len = kernel32.GetFullPathNameW(path, len, lpBuf, null)

                if (len > 0) {
                    return String(lpBuf, 0, len)
                } else {
                    throw Error("GetFullPathNameW failed")
                }
            }
        } else {
            throw WindowsException(Native.getLastError())
        }
    }
    private fun getFullPathName(
        path: String,
    ): String = getFullPathName0(path)

    private fun getAbsolutePathString(): String {
        if (!_parsed)
            parse()

        val normalized = _normalized!!

        if (isAbsolute())
            return normalized

        if (_isPathTypeRelative!!) {
            val defaultDirectory: String = fs.defaultDirectory.toString()
            if (normalized.isEmpty()) return defaultDirectory
            if (defaultDirectory.endsWith("\\")) {
                return defaultDirectory + normalized
            } else {
                val sb: StringBuilder =
                    StringBuilder(defaultDirectory.length + normalized.length + 1)
                return sb.append(defaultDirectory).append('\\').append(normalized).toString()
            }
        }

        if (_isPathTypeDirectoryRelative!!) {
            val defaultRoot: String = fs.defaultDirectory.root()!!.toString()
            return defaultRoot + normalized.toString().substring(1)
        }


        val root = _root!!
        // Drive relative path ("C:foo" for example).
        if (isSameDrive(root, fs.defaultDirectory.root()!!.toString())) {
            // relative to default directory
            val remaining: String = normalized.substring(root.length)
            val defaultDirectory: String = fs.defaultDirectory.root()!!.toString()
            return if (remaining.isEmpty()) {
                defaultDirectory
            } else if (defaultDirectory.endsWith("\\")) {
                defaultDirectory + remaining
            } else {
                defaultDirectory + "\\" + remaining
            }
        } else {
            // relative to some other drive
            val wd: String
            try {
                val dt = kernel32.GetDriveTypeW(WString(root + "\\"))
                if (dt == DRIVE_UNKNOWN || dt == DRIVE_NO_ROOT_DIR) throw WindowsException("")
                wd = getFullPathName(root + ".")
            } catch (x: WindowsException) {
                throw WindowsException("Unable to get working directory of drive '" + root[0].uppercaseChar() + "'")
            }
            var result = wd
            if (wd.endsWith("\\")) {
                result += normalized.substring(root.length)
            } else {
                if (normalized.length > root.length) result += "\\" + normalized.substring(root.length)
            }
            return result
        }
    }

    private fun getNormalizedPathString(): String {
        if (isPathNormalized)
            return path.toString()

        if (!_parsed)
            parse()
        return _normalized!!
    }

    val isPathTypeAbsolute: Boolean
        get() {
            return _isPathTypeAbsolute
                ?: run { parse() ; _isPathTypeAbsolute!! }
        }

    val isPathTypeUnc: Boolean
        get() {
            return _isPathTypeUnc
                ?: run { parse() ; _isPathTypeUnc!! }
        }

    val normalized: String
        get() {
            return _normalized
                ?: run { parse() ; _normalized!! }
        }

    fun pathForWin32Calls(allowShortPath: Boolean = true): String {
        if (!_parsed)
            parse()

        if (allowShortPath) {
            if (isAbsolute()) {
                val normalized = getNormalizedPathString()
                if (normalized.length <= MAX_DIRECTORY_PATH)
                    return normalized
            }
            _pathForWin32Calls?.let { return it }
        }

        var resolved = getAbsolutePathString()

        if (resolved.length > MAX_DIRECTORY_PATH || !allowShortPath) {
            if (resolved.length > MAX_LONG_PATH) {
                throw WindowsException(
                    ("Cannot access file with path exceeding "
                            + MAX_LONG_PATH + " characters")
                )
            }
            resolved = addPrefix(getFullPathName(resolved))
        }

        if (allowShortPath && !_isPathTypeDriveRelative!!) {
            _pathForWin32Calls = resolved
        }

        return resolved
    }

    companion object {
        private const val WIN32_MAX_PATH = 260
        private const val WIN32_MAX_LONG_PATH = 32767

        // WIN32_MAX_PATH (260) - 8.3 filename (12) - null terminator (1)
        private const val MAX_DIRECTORY_PATH = 247

        // WIN32_MAX_LONG_PATH (32767) - reasonable margin for os to use (767)
        private const val MAX_LONG_PATH = 32000


        // drive types
        const val DRIVE_UNKNOWN: Int = 0
        const val DRIVE_NO_ROOT_DIR: Int = 1

        private interface Kernel32 : Library {
            fun GetDriveTypeW(lpRootPathName: WString?): Int

            fun GetFullPathNameW(
                lpFileName: String,
                nBufferLength: Int,
                lpBuffer: CharArray,
                lpFilePart: PointerByReference? // We can pass null here as in the JNI code
            ): Int
        }

        private val kernel32: Kernel32 = Native.load("kernel32", Kernel32::class.java, W32APIOptions.DEFAULT_OPTIONS)

        private fun isSlash(c: Char): Boolean {
            return (c == '\\') || (c == '/')
        }

        private fun nextNonSlash(path: String, off: Int, end: Int): Int {
            for (i in off until end) {
                if (!isSlash(path[i]))
                    return i
            }
            return off;
        }

        private fun nextSlash(path: String, off: Int, end: Int): Int {
            for (i in off until end) {
                val c = path[i]
                if (isSlash(c)) {
                    return i
                }
                if (isInvalidPathChar(c)) {
                    throw InvalidPathException(path, "Illegal character [$c] in path", i)
                }
            }
            return end
        }

        private fun isLetter(c: Char): Boolean {
            return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
        }

        // Reserved characters for window path name
        private const val reservedChars: String = "<>:\"|?*"
        private fun isInvalidPathChar(ch: Char): Boolean {
            return ch < '\u0020' || reservedChars.indexOf(ch) != -1
        }

        // returns true if same drive letter
        private fun isSameDrive(root1: String, root2: String): Boolean {
            return root1[0].uppercaseChar() == root2[0].uppercaseChar()
        }

        // Add long path prefix to path
        fun addPrefix(path: String): String {
            var path = path
            if (path.startsWith("\\\\")) {
                path = "\\\\?\\UNC" + path.substring(1, path.length)
            } else {
                path = "\\\\?\\" + path
            }
            return path
        }
    }
}