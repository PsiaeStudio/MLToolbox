package dev.psiae.mltoolbox.shared.platform.win32

import com.sun.jna.Native
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinUser.WindowProc
import com.sun.jna.win32.W32APIOptions


interface User32Ex : com.sun.jna.Library {
    fun SetWindowLong(hWnd: HWND?, nIndex: Int, wndProc: WindowProc): LONG_PTR
    fun SetWindowLong(hWnd: HWND?, nIndex: Int, wndProc: LONG_PTR): LONG_PTR
    fun SetWindowLongPtr(hWnd: HWND?, nIndex: Int, wndProc: WindowProc): LONG_PTR
    fun SetWindowLongPtr(hWnd: HWND?, nIndex: Int, wndProc: LONG_PTR): LONG_PTR
    fun CallWindowProc(proc: LONG_PTR?, hWnd: HWND?, uMsg: Int, uParam: WPARAM?, lParam: LPARAM?): LRESULT
    fun SetWindowPos(
        hWnd: HWND?, hWndInsertAfter: HWND?, X: Int, Y: Int, cx: Int,
        cy: Int, uFlags: Int
    ): Boolean

    companion object {
        val INSTANCE = Native.load("user32", User32Ex::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }
}


object User32ExCompanion {
    const val GWLP_WNDPROC: Int = -4
}