package dev.psiae.mltoolbox.shared.platform.content.filepicker.win32


import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.COM.COMUtils.FAILED
import com.sun.jna.platform.win32.COM.IUnknown
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.Ole32.COINIT_APARTMENTTHREADED
import com.sun.jna.platform.win32.WinError.*
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import dev.psiae.mltoolbox.shared.platform.content.filepicker.win32.ShTypes.SIGDN.Companion.SIGDN_DESKTOPABSOLUTEPARSING
import java.awt.Window
import java.io.File


/**
 * io.github.vinceglb.filekit.core.platform.windows.WindowsFilePicker.kt
 */

/**
 * The native Windows folder browser.
 *
 * Example:
 * WindowsFolderBrowser fb = new WindowsFolderBrowser();
 * File dir = fb.showDialog(parentWindow);
 * if (dir != null) {
 * // do something with dir
 * }
 */
class WindowsFolderBrowser(
    var title: String? = null,
    var initialDirectory: String? = null,
) {

    val CLSID_FileOpenDialog = Guid.CLSID("{DC1C5A9C-E88A-4DDE-A5A1-60F82A20AEF7}")
    val IID_IFileOpenDialog = Guid.IID("{D57C7288-D4AD-4768-BE02-9D969532D960}")
    val IID_IShellItem = Guid.IID("{43826D1E-E718-42EE-BC55-A1E261C37BFE}")
    val FOS_PICKFOLDERS: Int = 0x20

    /**
     * displays the dialog to the user
     *
     * @param parentWindow the parent window
     *
     * @return the selected directory or null if the user canceled the dialog
     */
    fun showDialog(parentWindow: Window?): File? {
        var fileDialog: FileSaveDialog? = null
        var result: File? = null

        try {
            initCom()

            val pbrFileDialog = PointerByReference()
            fileDialog = Ole32.INSTANCE.CoCreateInstance(
                CLSID_FileOpenDialog,
                null,
                WTypes.CLSCTX_ALL,
                IID_IFileOpenDialog,
                pbrFileDialog
            )
                .verify("CoCreateInstance failed")
                .let { FileSaveDialog(pbrFileDialog.value) }

            initialDirectory?.let { fileDialog.setDefaultPath(it) }

            // Set title
            title?.let {
                fileDialog
                    .SetTitle(WString(title))
                    .verify("SetTitle failed")
            }

            // Add in FOS_PICKFOLDERS which hides files and only allows selection of folders
            fileDialog.setFlag(FOS_PICKFOLDERS)

            // Show the dialog to the user
            result = fileDialog.show(parentWindow) {
                it.getResult(SIGDN_DESKTOPABSOLUTEPARSING)
            }

        } finally {
            fileDialog?.Release()
            Ole32.INSTANCE.CoUninitialize()
        }

        return result
    }

    private fun initCom() {
        Ole32.INSTANCE.CoInitializeEx(
            null,
            COINIT_APARTMENTTHREADED or Ole32.COINIT_DISABLE_OLE1DDE
        ).verify("CoInitializeEx failed")

        val isInit = COMUtils.comIsInitialized()
        if (!isInit) {
            throw RuntimeException("COM initialization failed")
        }
    }

    private fun HRESULT.verify(exceptionMessage: String): HRESULT {
        if (FAILED(this)) {
            throw RuntimeException(exceptionMessage)
        } else {
            return this
        }
    }

    private fun Window?.toHwnd(): WinDef.HWND? {
        return when (this) {
            null -> null
            else -> Native
                .getWindowPointer(this)
                .let { WinDef.HWND(it) }
        }
    }

    internal interface IModalWindow : IUnknown {
        fun Show(hwndOwner: WinDef.HWND?): WinNT.HRESULT?

        companion object {
            val IID_IMODALWINDOW = Guid.IID("{b4db1657-70d7-485e-8e3e-6fcb5a5c1802}") // Guid.IID
        }
    }

    internal open class ModalWindow : Unknown, IModalWindow {
        constructor()

        constructor(pvInstance: Pointer?) : super(pvInstance)

        // VTBL Id indexing starts at 3 after Unknown's 0, 1, 2
        override fun Show(hwndOwner: WinDef.HWND?): WinNT.HRESULT {
            return _invokeNativeObject(
                3,
                arrayOf(this.pointer, hwndOwner),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }
    }

    internal interface IFileDialog : IModalWindow {
        fun SetFileTypes(
            FileTypes: Int,
            rgFilterSpec: Array<ShTypes.COMDLG_FILTERSPEC?>?
        ): WinNT.HRESULT?

        fun SetFileTypeIndex(iFileType: Int): WinNT.HRESULT?

        fun GetFileTypeIndex(piFileType: IntByReference?): WinNT.HRESULT?

        fun Advise(
            pfde: Pointer?,
            pdwCookie: IntByReference?
        ): WinNT.HRESULT? // IFileDialogEvents

        fun Unadvise(dwCookie: Int): WinNT.HRESULT?

        fun SetOptions(fos: Int): WinNT.HRESULT? // FILEOPENDIALOGOPTIONS

        fun GetOptions(pfos: IntByReference?): WinNT.HRESULT? // FILEOPENDIALOGOPTIONS

        fun SetDefaultFolder(psi: Pointer?): WinNT.HRESULT? // IShellItem

        fun SetFolder(psi: Pointer?): WinNT.HRESULT? // IShellItem

        fun GetFolder(ppsi: PointerByReference?): WinNT.HRESULT? // IShellItem

        fun GetCurrentSelection(ppsi: PointerByReference?): WinNT.HRESULT? // IShellItem

        fun SetFileName(pszName: WString?): WinNT.HRESULT?

        fun GetFileName(pszName: PointerByReference?): WinNT.HRESULT? // WString

        fun SetTitle(pszTitle: WString?): WinNT.HRESULT?

        fun SetOkButtonLabel(pszText: WString?): WinNT.HRESULT?

        fun SetFileNameLabel(pszLabel: WString?): WinNT.HRESULT?

        fun GetResult(ppsi: PointerByReference?): WinNT.HRESULT?

        fun AddPlace(psi: Pointer?, fdap: Int): WinNT.HRESULT? // IShellItem

        fun SetDefaultExtension(pszDefaultExtension: WString?): WinNT.HRESULT?

        fun Close(hr: WinNT.HRESULT?): WinNT.HRESULT?

        fun SetClientGuid(guid: Guid.GUID.ByReference?): WinNT.HRESULT?

        fun ClearClientData(): WinNT.HRESULT?

        fun SetFilter(pFilter: Pointer?): WinNT.HRESULT? // IShellItemFilter

        companion object {
            val IID_IFILEDIALOG = Guid.IID("{42f85136-db7e-439c-85f1-e4075d135fc8}") // Guid.IID
        }
    }

    internal interface IFileOpenDialog : IFileDialog {
        fun GetResults(ppenum: PointerByReference?): WinNT.HRESULT? // IShellItemArray

        fun GetSelectedItems(ppsai: PointerByReference?): WinNT.HRESULT? // IShellItemArray

        companion object {
            val IID_IFILEOPENDIALOG = Guid.IID("{d57c7288-d4ad-4768-be02-9d969532d960}")
            val CLSID_FILEOPENDIALOG = Guid.CLSID("{DC1C5A9C-E88A-4dde-A5A1-60F82A20AEF7}")
        }
    }

    internal open class FileDialog : ModalWindow, IFileDialog {
        constructor()

        constructor(pvInstance: Pointer?) : super(pvInstance)

        // VTBL Id indexing starts at 4 after ModalWindow's 3
        override fun SetFileTypes(
            FileTypes: Int,
            rgFilterSpec: Array<ShTypes.COMDLG_FILTERSPEC?>?
        ): WinNT.HRESULT {
            return _invokeNativeObject(
                4, arrayOf(this.pointer, FileTypes, rgFilterSpec),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetFileTypeIndex(iFileType: Int): WinNT.HRESULT {
            return _invokeNativeObject(
                5, arrayOf(this.pointer, iFileType),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetFileTypeIndex(piFileType: IntByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                6, arrayOf(this.pointer, piFileType),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun Advise(pfde: Pointer?, pdwCookie: IntByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                7, arrayOf(this.pointer, pfde, pdwCookie),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun Unadvise(dwCookie: Int): WinNT.HRESULT {
            return _invokeNativeObject(
                8, arrayOf(this.pointer, dwCookie),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetOptions(fos: Int): WinNT.HRESULT {
            return _invokeNativeObject(
                9,
                arrayOf(this.pointer, fos),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetOptions(pfos: IntByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                10, arrayOf(this.pointer, pfos),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetDefaultFolder(psi: Pointer?): WinNT.HRESULT {
            return _invokeNativeObject(
                11, arrayOf<Any?>(this.pointer, psi),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetFolder(psi: Pointer?): WinNT.HRESULT {
            return _invokeNativeObject(
                12, arrayOf<Any?>(this.pointer, psi),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetFolder(ppsi: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                13, arrayOf(this.pointer, ppsi),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetCurrentSelection(ppsi: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                14, arrayOf(this.pointer, ppsi),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetFileName(pszName: WString?): WinNT.HRESULT {
            return _invokeNativeObject(
                15, arrayOf(this.pointer, pszName),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetFileName(pszName: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                16, arrayOf(this.pointer, pszName),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetTitle(pszTitle: WString?): WinNT.HRESULT {
            return _invokeNativeObject(
                17, arrayOf(this.pointer, pszTitle),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetOkButtonLabel(pszText: WString?): WinNT.HRESULT {
            return _invokeNativeObject(
                18, arrayOf(this.pointer, pszText),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetFileNameLabel(pszLabel: WString?): WinNT.HRESULT {
            return _invokeNativeObject(
                19, arrayOf(this.pointer, pszLabel),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetResult(ppsi: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                20, arrayOf(this.pointer, ppsi),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun AddPlace(psi: Pointer?, fdap: Int): WinNT.HRESULT {
            return _invokeNativeObject(
                21, arrayOf(this.pointer, psi, fdap),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetDefaultExtension(pszDefaultExtension: WString?): WinNT.HRESULT {
            return _invokeNativeObject(
                22, arrayOf(this.pointer, pszDefaultExtension),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun Close(hr: WinNT.HRESULT?): WinNT.HRESULT {
            return _invokeNativeObject(
                23, arrayOf(this.pointer, hr),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetClientGuid(guid: Guid.GUID.ByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                24, arrayOf(this.pointer, guid),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun ClearClientData(): WinNT.HRESULT {
            return _invokeNativeObject(
                25, arrayOf<Any>(this.pointer),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetFilter(pFilter: Pointer?): WinNT.HRESULT {
            return _invokeNativeObject(
                26, arrayOf<Any?>(this.pointer, pFilter),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }
    }

    internal class FileOpenDialog : FileDialog, IFileOpenDialog {
        constructor()

        constructor(pvInstance: Pointer?) : super(pvInstance)

        // VTBL Id indexing starts at 27 after IFileDialog's 26
        override fun GetResults(ppenum: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                27,
                arrayOf(this.pointer, ppenum),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetSelectedItems(ppsai: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                28,
                arrayOf(this.pointer, ppsai),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }
    }

    internal interface IFileSaveDialog : IFileDialog {
        fun SetSaveAsItem(psi: Pointer?): WinNT.HRESULT?

        fun SetProperties(pStore: Pointer?): WinNT.HRESULT? // IPropertyStore

        fun SetCollectedProperties(
            pList: Pointer?,
            fAppendDefault: Boolean
        ): WinNT.HRESULT? // IPropertyDescriptionList

        fun GetProperties(ppStore: PointerByReference?): WinNT.HRESULT? // IPropertyStore

        fun ApplyProperties(
            psi: Pointer?,
            pStore: Pointer?,
            hwnd: WinDef.HWND?,
            pSink: Pointer?
        ): WinNT.HRESULT? // IShellItem, IPropertyStore, HWND, IFileOperationProgressSink

        companion object {
            val IID_IFILESAVEDIALOG = Guid.IID("{84bccd23-5fde-4cdb-aea4-af64b83d78ab}")
            val CLSID_FILESAVEDIALOG = Guid.CLSID("{C0B4E2F3-BA21-4773-8DBA-335EC946EB8B}")
        }
    }

    internal class FileSaveDialog : FileDialog, IFileSaveDialog {
        constructor()

        constructor(pvInstance: Pointer?) : super(pvInstance)

        // VTBL Id indexing starts at 27 after IFileDialog's 26
        override fun SetSaveAsItem(psi: Pointer?): WinNT.HRESULT {
            return _invokeNativeObject(
                27, arrayOf<Any?>(this.pointer, psi),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetProperties(pStore: Pointer?): WinNT.HRESULT {
            return _invokeNativeObject(
                28, arrayOf<Any?>(this.pointer, pStore),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun SetCollectedProperties(
            pList: Pointer?,
            fAppendDefault: Boolean
        ): WinNT.HRESULT {
            return _invokeNativeObject(
                29, arrayOf(this.pointer, pList, fAppendDefault),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetProperties(ppStore: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                30, arrayOf(this.pointer, ppStore),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun ApplyProperties(
            psi: Pointer?,
            pStore: Pointer?,
            hwnd: WinDef.HWND?,
            pSink: Pointer?
        ): WinNT.HRESULT {
            return _invokeNativeObject(
                31, arrayOf(this.pointer, psi, pStore, hwnd, pSink),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }
    }

    internal interface IShellItem : IUnknown {
        fun BindToHandler(
            pbc: Pointer?,
            bhid: Guid.GUID.ByReference?,
            riid: Guid.REFIID?,
            ppv: PointerByReference?
        ): WinNT.HRESULT? // IBindCtx

        fun GetParent(
            ppsi: PointerByReference?
        ): WinNT.HRESULT? // IShellItem

        fun GetDisplayName(
            sigdnName: Long,
            ppszName: PointerByReference?
        ): WinNT.HRESULT? // SIGDN, WString

        fun GetAttributes(
            sfgaoMask: Int,
            psfgaoAttribs: IntByReference?
        ): WinNT.HRESULT? // SFGAOF, SFGAOF

        fun Compare(
            psi: Pointer?,
            hint: Int,
            piOrder: IntByReference?
        ): WinNT.HRESULT? // IShellItem , SICHINTF

        companion object {
            val IID_ISHELLITEM = Guid.IID("{43826d1e-e718-42ee-bc55-a1e261c37bfe}") // Guid.IID
            val CLSID_SHELLITEM = Guid.CLSID("{9ac9fbe1-e0a2-4ad6-b4ee-e212013ea917}") // Guid.CLSID
        }
    }

    internal class ShellItem : Unknown, IShellItem {
        constructor()

        constructor(pvInstance: Pointer?) : super(pvInstance)

        // VTBL Id indexing starts at 3 after Unknown's 0, 1, 2
        override fun BindToHandler(
            pbc: Pointer?,
            bhid: Guid.GUID.ByReference?,
            riid: Guid.REFIID?,
            ppv: PointerByReference?
        ): WinNT.HRESULT {
            return _invokeNativeObject(
                3, arrayOf(this.pointer, pbc, bhid, riid, ppv),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetParent(ppsi: PointerByReference?): WinNT.HRESULT {
            return _invokeNativeObject(
                4, arrayOf(this.pointer, ppsi),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetDisplayName(
            sigdnName: Long,
            ppszName: PointerByReference?
        ): WinNT.HRESULT {
            return _invokeNativeObject(
                5, arrayOf(this.pointer, sigdnName, ppszName),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun GetAttributes(
            sfgaoMask: Int,
            psfgaoAttribs: IntByReference?
        ): WinNT.HRESULT {
            return _invokeNativeObject(
                6, arrayOf(this.pointer, sfgaoMask, psfgaoAttribs),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }

        override fun Compare(
            psi: Pointer?,
            hint: Int,
            piOrder: IntByReference?
        ): WinNT.HRESULT {
            return _invokeNativeObject(
                7, arrayOf(this.pointer, psi, hint, piOrder),
                WinNT.HRESULT::class.java
            ) as WinNT.HRESULT
        }
    }

    private fun FileDialog.setDefaultPath(defaultPath: String) {
        val pbrFolder = PointerByReference()
        val resultFolder = Shell32.SHCreateItemFromParsingName(
            WString(defaultPath),
            null,
            Guid.REFIID(IShellItem.IID_ISHELLITEM),
            pbrFolder
        )

        // Valid error code: File not found
        val fileNotFoundException = Win32Exception(ERROR_FILE_NOT_FOUND)
        if (resultFolder == fileNotFoundException.hr) {
            println("FileKit - Initial directory not found: ${fileNotFoundException.message}")
            return
        }

        // Valid error code: Invalid drive
        val invalidDriveException = Win32Exception(ERROR_INVALID_DRIVE)
        if (resultFolder == invalidDriveException.hr) {
            println("FileKit - Invalid drive: ${invalidDriveException.message}")
            return
        }

        // Invalid error codes: throw exception
        if (FAILED(resultFolder)) {
            throw RuntimeException("SHCreateItemFromParsingName failed")
        }

        // Create ShellItem from the folder
        val folder = ShellItem(pbrFolder.value)

        // Set the initial directory
        this.SetFolder(folder.pointer)

        // Release the folder
        folder.Release()
    }

    private fun FileDialog.setFlag(flag: Int) {
        // Get the dialog options
        val ref = IntByReference()
        this.GetOptions(ref).verify("GetOptions failed")

        // Set the dialog options
        this.SetOptions(ref.value or flag).verify("SetOptions failed")
    }

    private fun <T> FileDialog.show(
        parentWindow: Window?,
        block: (FileDialog) -> T
    ): T? {
        // Show the dialog to the user
        val openDialogResult = this.Show(parentWindow.toHwnd())

        // Valid error code: User canceled the dialog
        val userCanceledException = Win32Exception(ERROR_CANCELLED)
        if (openDialogResult == userCanceledException.hr) {
            return null
        }

        // Invalid error codes: throw exception
        if (FAILED(openDialogResult)) {
            throw RuntimeException("Show failed")
        }

        return block(this)
    }

    private fun FileDialog.getResult(sigdnName: Long): File {
        var item: ShellItem? = null
        var pbrDisplayName: PointerByReference? = null

        try {
            // Get the selected item
            val pbrItem = PointerByReference()
            this
                .GetResult(pbrItem)
                .verify("GetResult failed")

            // Create ShellItem from the pointer
            item = ShellItem(pbrItem.value)

            // Get the display name
            pbrDisplayName = PointerByReference()
            item
                .GetDisplayName(sigdnName, pbrDisplayName)
                .verify("GetDisplayName failed")

            // Get the path
            val path = pbrDisplayName.value.getWideString(0)

            // Return the file
            return File(path)
        } finally {
            // Release
            pbrDisplayName?.let { Ole32.INSTANCE.CoTaskMemFree(it.value) }
            item?.Release()
        }
    }
}