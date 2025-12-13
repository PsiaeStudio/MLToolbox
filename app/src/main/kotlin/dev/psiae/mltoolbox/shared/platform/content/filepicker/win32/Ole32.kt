package dev.psiae.mltoolbox.shared.platform.content.filepicker.win32

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.PointerByReference


object Ole32 {
    init {
        Native.register("ole32")
    }

    const val COINIT_APARTMENTTHREADED = 0x2
    const val COINIT_MULTITHREADED = 0x0

    external fun OleInitialize(pvReserved: Pointer?): Pointer?
    external fun CoInitializeEx(reserved: Pointer?, coinit: Int): WinNT.HRESULT
    external fun CoCreateInstance(clsid: Guid.CLSID, pUnkOuter: Pointer?, dwClsContext: Int, riid: Guid.IID, ppv: PointerByReference): WinNT.HRESULT
    external fun CoTaskMemFree(pv: Pointer)
}