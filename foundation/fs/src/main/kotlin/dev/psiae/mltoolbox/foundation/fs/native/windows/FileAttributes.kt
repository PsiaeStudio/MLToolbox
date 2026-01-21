package dev.psiae.mltoolbox.foundation.fs.native.windows

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.win32.W32APIOptions
import dev.psiae.mltoolbox.foundation.fs.internal.path.isAbsolute
import dev.psiae.mltoolbox.foundation.fs.path.Path
import dev.psiae.mltoolbox.foundation.fs.path.backslashSeparatorsPathString
import java.io.IOException

internal object WindowsNativeFileAttributes {

    private interface Kernel32 : Library {
        fun CreateFileW(
            lpFileName: String,
            dwDesiredAccess: Int,
            dwShareMode: Int,
            lpSecurityAttributes: Pointer?,
            dwCreationDisposition: Int,
            dwFlagsAndAttributes: Int,
            hTemplateFile: Pointer?
        ): WinNT.HANDLE

        fun SetFileAttributesW(lpFileName: String, dwFileAttributes: Int): Boolean
        fun GetFileAttributesW(lpFileName: String): Int

        fun CloseHandle(hObject: WinNT.HANDLE): Boolean
    }

    private val kernel32: Kernel32 = Native.load("kernel32", Kernel32::class.java, W32APIOptions.DEFAULT_OPTIONS)
    private const val FILE_WRITE_ATTRIBUTES = 0x0100
    private const val OPEN_EXISTING = 3

    private const val FILE_SHARE_READ = 0x00000001
    private const val FILE_SHARE_WRITE = 0x00000002
    private const val FILE_SHARE_DELETE = 0x00000004
    private const val FILE_SHARE_ALL = FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE

    private val INVALID_HANDLE = WinNT.HANDLE(Pointer.createConstant(-1L))

    @Throws(IOException::class)
    fun checkCanWriteAttributes(
        path: WindowsPath
    ) {
        try {
            val handle = kernel32.CreateFileW(
                path.pathForWin32Calls(),
                FILE_WRITE_ATTRIBUTES,
                FILE_SHARE_ALL,
                null,
                OPEN_EXISTING,
                0,
                null
            )

            if (handle == INVALID_HANDLE) {
                throw Win32Errors.translateToFsException(path, null, Native.getLastError())
            }

            kernel32.CloseHandle(handle)
        } catch (w: WindowsException) {
            throw Win32Errors.translateToFsException(path, null, w.lastError())
        }
    }

    @Throws(IOException::class)
    fun writeSameAttributes(path: WindowsPath): Boolean {
        val lpFileName = path.pathForWin32Calls()
        return kernel32.SetFileAttributesW(
            lpFileName,
            kernel32.GetFileAttributesW(lpFileName)
        )
    }
}