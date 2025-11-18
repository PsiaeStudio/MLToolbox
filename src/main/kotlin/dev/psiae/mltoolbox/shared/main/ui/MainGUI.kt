package dev.psiae.mltoolbox.shared.main.ui

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.main.ui.composeui.querySystemOSBuildVersionStr
import kotlinx.atomicfu.locks.withLock
import java.awt.Dimension
import java.awt.Insets
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.util.concurrent.locks.ReentrantLock
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JTextArea
import javax.swing.KeyStroke
import javax.swing.UIManager

// TODO: FancySimpleErrorWindow, default as fallback

fun MainGUI(
    app: MLToolboxApp
) = dev.psiae.mltoolbox.shared.main.ui.composeui.MainGUI(app)


private val reentrantLock = ReentrantLock()
private var isExceptionWindowLocked = false
fun tryLockExceptionWindow(): Boolean {
    reentrantLock.withLock {
        if (isExceptionWindowLocked)
            return false
        isExceptionWindowLocked = true
        return true
    }
}


val DefaultSimpleErrorWindow = { errorMsg: String ->
    JOptionPane.showMessageDialog(JFrame().apply { size = Dimension(300, 300) },
        JTextArea()
            .apply {
                text = errorMsg
            },
        "MLToolbox",
        JOptionPane.ERROR_MESSAGE);
}


// TODO: FancyExceptionWindow, default as fallback

val DefaultExceptionWindow = { errorMsg: String, throwable: Throwable? ->
    JOptionPane.showMessageDialog(
        JFrame().apply {
            size = Dimension(300, 300)
        },
        JTextArea()
            .apply {
                val renderText = try {
                    throwable?.stackTraceToString()
                        ?: ""
                } catch (t: Throwable) {
                    try {
                        throwable.toString()
                    } catch (t: Throwable) {
                        try {
                            throwable?.javaClass?.name ?: "NoStackTrace (null)"
                        } catch (t: Throwable) {
                            "NoStackTrace"
                        }
                    }
                }
                val textBuilder = StringBuilder()
                    .apply {
                        append("Error")
                        if (errorMsg.isNotEmpty()) {
                            append(": $errorMsg")
                            append("\n\n")
                        }
                        append(renderText)

                        run {
                            val jreName = System.getProperty("java.runtime.name")
                            val jreVersion = System.getProperty("java.runtime.version")
                            val jvmName = System.getProperty("java.vm.name")
                            val jvmVersion = System.getProperty("java.vm.version")

                            append("\n")
                            append("\n$jreName (build $jreVersion)")
                            append("\n$jvmName (build $jvmVersion)")

                            runCatching {
                                append("\n\n${querySystemOSBuildVersionStr()}")
                            }
                        }

                        append("\n\nCTRL + C  to copy")
                    }
                text = textBuilder.toString()
                isEditable = false
                lineWrap = false
                margin = Insets(8, 8, 8, 8)
                setFont(UIManager.getDefaults().getFont("JOptionPane.font"))
                // Add a Ctrl+C action to copy the error message to clipboard
                val copyKeyStroke: KeyStroke =
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx)
                getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(copyKeyStroke, "copy")
                getActionMap().put("copy", object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        val selection = StringSelection(text)
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
                    }
                })
            },
        "MLToolbox",
        JOptionPane.ERROR_MESSAGE);
}