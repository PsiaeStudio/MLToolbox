package dev.psiae.mltoolbox.foundation.fs.native.windows

import dev.psiae.mltoolbox.foundation.fs.AccessDeniedException
import dev.psiae.mltoolbox.foundation.fs.FileAlreadyExistsException
import dev.psiae.mltoolbox.foundation.fs.FileSystemException
import dev.psiae.mltoolbox.foundation.fs.NoSuchFileException
import dev.psiae.mltoolbox.foundation.fs.path.Path

internal object Win32Errors {
    const val ERROR_FILE_NOT_FOUND: Int = 2
    const val ERROR_PATH_NOT_FOUND: Int = 3
    const val ERROR_ACCESS_DENIED: Int = 5
    const val ERROR_INVALID_HANDLE: Int = 6
    const val ERROR_INVALID_DATA: Int = 13
    const val ERROR_NOT_SAME_DEVICE: Int = 17
    const val ERROR_NOT_READY: Int = 21
    const val ERROR_SHARING_VIOLATION: Int = 32
    const val ERROR_FILE_EXISTS: Int = 80
    const val ERROR_INVALID_PARAMETER: Int = 87
    const val ERROR_DISK_FULL: Int = 112
    const val ERROR_INSUFFICIENT_BUFFER: Int = 122
    const val ERROR_INVALID_LEVEL: Int = 124
    const val ERROR_DIR_NOT_ROOT: Int = 144
    const val ERROR_DIR_NOT_EMPTY: Int = 145
    const val ERROR_ALREADY_EXISTS: Int = 183
    const val ERROR_MORE_DATA: Int = 234
    const val ERROR_DIRECTORY: Int = 267
    const val ERROR_NOTIFY_ENUM_DIR: Int = 1022
    const val ERROR_PRIVILEGE_NOT_HELD: Int = 1314
    const val ERROR_NONE_MAPPED: Int = 1332
    const val ERROR_CANT_ACCESS_FILE: Int = 1920
    const val ERROR_NOT_A_REPARSE_POINT: Int = 4390
    const val ERROR_INVALID_REPARSE_DATA: Int = 4392

    fun translateToFsException(
        path: Path,
        otherPath: Path?,
        errorCode: Int,
    ): FileSystemException = when (errorCode) {
        ERROR_FILE_NOT_FOUND, ERROR_PATH_NOT_FOUND -> NoSuchFileException(path, otherPath)
        ERROR_FILE_EXISTS, ERROR_ALREADY_EXISTS -> FileAlreadyExistsException(path, otherPath)
        ERROR_ACCESS_DENIED -> AccessDeniedException(path, otherPath)
        else -> FileSystemException(path, otherPath)
    }
}