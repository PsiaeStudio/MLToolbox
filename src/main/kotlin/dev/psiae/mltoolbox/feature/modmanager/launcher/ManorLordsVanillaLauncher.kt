package dev.psiae.mltoolbox.feature.modmanager.launcher

import com.sun.jna.platform.win32.Advapi32
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT.*
import com.sun.jna.ptr.IntByReference
import dev.psiae.mltoolbox.shared.java.jFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.nio.file.Files


class ManorLordsVanillaLauncher(
    val coroutineScope: CoroutineScope,
    val targetGameBinary: jFile,
) {

    private var unrealGameRoot: jFile? = null
    private var gameRoot: jFile? = null

    fun launch(

    ) {
        coroutineScope.launch {
            lazyResolveUnrealGameDirectories()
            println("unrealGameRoot=$unrealGameRoot, gameRoot=$gameRoot")
        }
    }

    private fun lazyResolveUnrealGameDirectories() {
        if (unrealGameRoot == null || gameRoot == null) {
            resolveUnrealGameDirectories()
        }
    }

    private fun resolveUnrealGameDirectories() {
        val (unrealGameRoot, gameRoot) = targetGameBinary.absolutePath
            .split("\\")
            .let { split ->
                if (split.size < 5) {
                    error("unable to find target game binary root directory, split size too small=${split.size}")
                }
                split.dropLast(4).joinToString("\\") to split.dropLast(3).joinToString("\\")
            }
        this.unrealGameRoot = jFile(unrealGameRoot)
        this.gameRoot = jFile(gameRoot)

        if (isRunAsAdmin()) {
            println("isRunAsAdmin=true")
            jFile("${System.getProperty("user.dir")}\\mltoolboxapp\\launcher\\vanilla").mkdirs()
            val create = Files.createSymbolicLink(
                jFile("${System.getProperty("user.dir")}\\mltoolboxapp\\launcher\\vanilla\\1\\").toPath(),
                jFile(unrealGameRoot).toPath(),
            )
            println("create=$create")
        } else {
            println("isRunAsAdmin=false")
        }
    }

    fun isRunAsAdmin(): Boolean {
        // get current process handle
        val hProcess = Kernel32.INSTANCE.GetCurrentProcess()
        val hToken = HANDLEByReference()

        // open process token
        var success = Advapi32.INSTANCE.OpenProcessToken(
            hProcess,
            TOKEN_QUERY,
            hToken
        )

        if (!success) {
            // fail
            error("isRunAsAdmin: Advapi32.INSTANCE.OpenProcessToken fail")
            return false
        }

        try {
            // Retrieve token elevation information
            val elevation = TOKEN_ELEVATION()
            val size = IntByReference()

            success = Advapi32.INSTANCE.GetTokenInformation(
                hToken.value,
                TOKEN_INFORMATION_CLASS.TokenElevation,
                elevation,
                elevation.size(),
                size
            )

            if (!success) {
                // Could not get token information
                return false
            }

            // Read the elevation information
            elevation.read()

            // TokenIsElevated is non-zero if the process is elevated
            return elevation.TokenIsElevated != 0
        } finally {
            // Close the token handle
            Kernel32.INSTANCE.CloseHandle(hToken.value)
        }
    }

}