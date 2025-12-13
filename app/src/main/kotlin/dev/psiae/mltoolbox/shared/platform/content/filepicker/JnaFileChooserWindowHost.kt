package dev.psiae.mltoolbox.shared.platform.content.filepicker

import dev.psiae.mltoolbox.shared.platform.content.filepicker.win32.JnaFileChooser
import kotlinx.coroutines.*
import java.awt.Window
import java.io.File

class JnaFileChooserWindowHost(
    private val parentWindow: Window,
    private val initialTitle: String,
    private val initialDir: String?,
    private val initialFileName: String = "",
    private val filters: () -> List<Pair<String, Array<String>>>? = { null },
    private val mode: JnaFileChooser.Mode,
    private val multiSelect: Boolean
) : Window(parentWindow) {
    private val jnaFileChooser = JnaFileChooser()
    private val coroutineScope = CoroutineScope(SupervisorJob())
    private var current: Deferred<Result<File?>>? = null
    private var showSaveAs = false
    private var buttonText = ""

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
    }

    override fun dispose() {
        super.dispose()
        coroutineScope.cancel()
        current?.cancel()
    }

    fun toSaveAs(
        buttonText: String = ""
    ) {
        showSaveAs = true
        this.buttonText = buttonText
    }

    fun openAndInvokeOnCompletion(
        handle: (Result<File?>) -> Unit
    ): DisposableHandle {
        isVisible = true
        val task = current?.takeIf { it.isActive }
            ?: run {
                coroutineScope.async(Dispatchers.IO) {
                    runCatching {
                        jnaFileChooser.setTitle(initialTitle)
                        jnaFileChooser.setCurrentDirectory(initialDir)
                        jnaFileChooser.setDefaultFileName(initialFileName)
                        val filter = filters.invoke()
                        filter?.forEach { (name, patterns) ->
                            jnaFileChooser.addFilter(name, *patterns)
                        }
                        jnaFileChooser.mode = mode
                        jnaFileChooser.isMultiSelectionEnabled = multiSelect
                        jnaFileChooser.setSaveButtonText(buttonText)
                        if (showSaveAs) {
                            jnaFileChooser.showSaveDialog(parent = this@JnaFileChooserWindowHost)
                        } else {
                            jnaFileChooser.showOpenDialog(parent = this@JnaFileChooserWindowHost)
                        }
                        jnaFileChooser.selectedFiles.first()
                    }
                }
            }.also { current = it }

        @OptIn(InternalCoroutinesApi::class)
        return task.invokeOnCompletion(onCancelling = true, invokeImmediately = true) { ex ->
            if (ex != null) handle.invoke(Result.failure(ex)) else handle.invoke(task.getCompleted())
        }
    }
}