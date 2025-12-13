package dev.psiae.mltoolbox.shared.main.ui.composeui

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.application
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.core.logger.Logger
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import kotlin.system.exitProcess


fun MainGUI(
    app: MLToolboxApp
) {
    application {
        // we should already be in Swing EQ
        // let compose manage the lifecycle

        AwtWindow(
            visible = true,
            create = {
                MainAwtWindow()
                    .apply {
                        minimumSize = Dimension(16 * 42, 9 * 42)
                        size = Dimension(16 * 100, 9 * 100)

                        // first show at center
                        run {
                            val screenInsets = Toolkit
                                .getDefaultToolkit()
                                .getScreenInsets(graphicsConfiguration)
                            val screenBounds = graphicsConfiguration.bounds
                            val size = IntSize(size.width, size.height)
                            val screenSize = IntSize(
                                screenBounds.width - screenInsets.left - screenInsets.right,
                                screenBounds.height - screenInsets.top - screenInsets.bottom
                            )
                            val location = Alignment.Center.align(size, screenSize, LayoutDirection.Ltr)

                            setLocation(
                                screenBounds.x + screenInsets.left + location.x,
                                screenBounds.y + screenInsets.top + location.y
                            )
                        }
                    }
                    .apply {
                        addWindowListener(
                            object : WindowListener {
                                override fun windowOpened(e: WindowEvent?) {
                                }
                                override fun windowClosing(e: WindowEvent?) {
                                    Logger.tryLog {"windowClosing, exiting with status code: '0'"}
                                    exitProcess(0)
                                }
                                override fun windowClosed(e: WindowEvent?) {
                                }
                                override fun windowIconified(e: WindowEvent?) {
                                }
                                override fun windowDeiconified(e: WindowEvent?) {
                                }
                                override fun windowActivated(e: WindowEvent?) {
                                }
                                override fun windowDeactivated(e: WindowEvent?) {
                                }
                            }
                        )
                    }
            },
            dispose = {
                it.dispose()
            },
            update = {
            },
        )
    }
}


fun querySystemOSBuildVersionStr(): String {
    var buildVersionStr = "UNKNOWN_OS"
    val osName = System.getProperty("os.name")
    when {
        osName.startsWith("Windows") -> {
            val out = StringBuilder()
            Runtime.getRuntime().exec("cmd /c ver").inputStream
                .bufferedReader().readLines().forEachIndexed { i, line ->
                    with(out) {
                        if (isNotEmpty())
                            if (i == 0) append("    ") else append(" ")
                        append(line)
                    }
                }
            buildVersionStr = out.toString().ifBlank { "Windows" }
        }
        osName.startsWith("Mac") -> {
            val out = StringBuilder()
            Runtime.getRuntime().exec("sw_vers -productName").inputStream
                .bufferedReader().readLines().forEachIndexed { i, line ->
                    with(out) {
                        if (isNotEmpty())
                            if (i == 0) append("    ") else append(" ")
                        append(line)
                    }
                }

            Runtime.getRuntime().exec("sw_vers -productVersion").inputStream
                .bufferedReader().readLines().forEachIndexed { i, line ->
                    with(out) {
                        if (isNotEmpty())
                            if (i == 0) append("    ") else append(" ")
                        append(line)
                    }
                }

            buildVersionStr = out.toString().ifBlank { "Mac" }
        }
        osName.startsWith("Linux") || osName.startsWith("LINUX") -> {
            val out = StringBuilder()
            val reader = jFile("/etc/os-release").bufferedReader()
            var line = reader.readLine()
            while (line != null) {
                if (line.startsWith("PRETTY_NAME=")) {
                    out.append(line.drop("PRETTY_NAME=".length).replace("\"", ""))
                    break
                }
                line = reader.readLine()
            }
            buildVersionStr = out.toString().ifBlank { "Linux" }
        }
    }
    return buildVersionStr
}