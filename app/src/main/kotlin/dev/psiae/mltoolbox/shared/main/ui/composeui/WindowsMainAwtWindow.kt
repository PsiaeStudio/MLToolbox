package dev.psiae.mltoolbox.shared.main.ui.composeui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowExceptionHandler
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.core.coroutine.AppCoroutineDispatchers
import dev.psiae.mltoolbox.foundation.ui.ScreenContextImpl
import dev.psiae.mltoolbox.foundation.ui.compose.DispatchContextImpl
import dev.psiae.mltoolbox.foundation.ui.compose.LocalScreenContext
import dev.psiae.mltoolbox.shared.ui.compose.theme.md3.colorScheme
import dev.psiae.mltoolbox.core.logger.Logger
import dev.psiae.mltoolbox.shared.main.ui.DefaultExceptionWindow
import dev.psiae.mltoolbox.shared.main.ui.tryLockExceptionWindow
import dev.psiae.mltoolbox.shared.ui.UiFoundation
import dev.psiae.mltoolbox.shared.ui.compose.CustomWin32TitleBarBehavior
import dev.psiae.mltoolbox.shared.ui.compose.LocalApplication
import dev.psiae.mltoolbox.foundation.ui.compose.LocalAwtWindow
import dev.psiae.mltoolbox.foundation.ui.compose.LocalComponentContext
import dev.psiae.mltoolbox.shared.ui.mainImmediateUiDispatcher
import dev.psiae.mltoolbox.shared.ui.compose.LocalComposeApplicationScope
import dev.psiae.mltoolbox.shared.ui.compose.LocalTitleBarBehavior
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.UIManager
import kotlin.system.exitProcess


var GLOBAL_IS_SYSTEM_DARK_THEME by mutableStateOf(false)

class WindowsMainAwtWindow(
    internal val applicationScope: ApplicationScope
) : DesktopMainAwtWindow() {

    internal val titleBarBehavior = CustomWin32TitleBarBehavior(
        this,
        onCloseClicked = applicationScope::exitApplication
    )

    private val pane = ComposePanel()

    private var windowHandle: Long? = null

    init {

        System.setProperty("compose.swing.render.on.graphics", "true")
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (e: Exception) {
        }
        val window = this


        @OptIn(ExperimentalComposeUiApi::class)
        pane.exceptionHandler = run {
            WindowExceptionHandler { thr ->
                runCatching {
                    Logger.tryLog { "Exception reached pane, in thread '${Thread.currentThread().name}': ${thr.stackTraceToString()}" }
                    val useExceptionWindow = tryLockExceptionWindow()
                    if (!useExceptionWindow)
                        return@WindowExceptionHandler
                    DefaultExceptionWindow("Exception reached pane, in thread '${Thread.currentThread().name}'", thr)
                }
                exitProcess(1)
            }
        }

        GLOBAL_IS_SYSTEM_DARK_THEME = currentSystemTheme == SystemTheme.DARK

        isUndecorated = false
        pane.setContent {

            LaunchedEffect(Unit) {
                while(currentCoroutineContext().isActive) {
                    GLOBAL_IS_SYSTEM_DARK_THEME = currentSystemTheme == SystemTheme.DARK
                    delay(1000)
                }
            }
            ProvideApplicationCompositionLocals(
                applicationScope
            ) {
                ProvideCoreUICompositionLocals {
                    ProvideWinCoreUICompositionLocals(
                        awtWindowWrapper = this@WindowsMainAwtWindow,
                    ) {
                        MainScreen()
                    }
                }
            }
        }

        contentPane.add(pane)

        window
            .apply {
                title = "ManorLords Toolbox"
                iconImage = run {
                    val resourcePath = "drawable/icon_manorlords_logo_text.png"
                    val contextClassLoader = Thread.currentThread().contextClassLoader!!
                    val resource = contextClassLoader.getResourceAsStream(resourcePath)
                    requireNotNull(resource) {
                        "Resource $resourcePath not found"
                    }.use(::loadImageBitmap).toAwtImage()
                }

                titleBarBehavior.apply {
                    onResized()
                    onMoved()
                }

                addWindowStateListener { wE ->
                    if (window.extendedState == MAXIMIZED_BOTH) {
                        titleBarBehavior.onMaximizedBoth()
                    } else if (window.extendedState == NORMAL) {
                        titleBarBehavior.onRestore()
                    }
                }

                addComponentListener(
                    object : ComponentListener {
                        override fun componentResized(p0: ComponentEvent?) {
                            titleBarBehavior.onResized()
                        }

                        override fun componentMoved(p0: ComponentEvent?) {
                            titleBarBehavior.onMoved()
                        }

                        override fun componentShown(p0: ComponentEvent?) {
                        }

                        override fun componentHidden(p0: ComponentEvent?) {
                        }

                    }
                )
            }
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        if (isDisplayable)
            titleBarBehavior.init(hWnd())
    }

    private fun lazyWindowHandle(): Long =
        windowHandle
            ?: Pointer
                .nativeValue(Native.getWindowPointer(this))
                .also { windowHandle = it }

    private fun hWnd(): WinDef.HWND =
        WinDef.HWND()
            .apply { pointer = Pointer(lazyWindowHandle()) }
}



@Composable
private fun ProvideApplicationCompositionLocals(
    applicationScope: ApplicationScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalApplication provides MLToolboxApp.requireInstance(),
        LocalComposeApplicationScope provides applicationScope,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvideBasePlatformCoreUICompositionLocals(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MD3Theme.colorScheme
    ) {
        CompositionLocalProvider(
            content = content
        )
    }
}

@Composable
private fun ProvideDesktopCoreUICompositionLocals(
    content: @Composable () -> Unit
) {
    ProvideBasePlatformCoreUICompositionLocals(

    ) {
        CompositionLocalProvider(
            content = content
        )
    }
}

@Composable
private fun ProvideWinCoreUICompositionLocals(
    awtWindowWrapper: WindowsMainAwtWindow,
    content: @Composable () -> Unit
) {
    ProvideDesktopCoreUICompositionLocals(

    ) {
        CompositionLocalProvider(
            LocalAwtWindow provides awtWindowWrapper,
            LocalTitleBarBehavior provides awtWindowWrapper.titleBarBehavior,
            content = content
        )
    }
}


@Composable
private fun ProvideCoreUICompositionLocals(
    content: @Composable () -> Unit
) {
    val screenContext = remember {
        ScreenContextImpl(
            dispatch = DispatchContextImpl(
                mainDispatcher = UiFoundation.mainImmediateUiDispatcher,
                ioDispatcher = AppCoroutineDispatchers.io
            ),
            lifetime = SupervisorJob(MLToolboxApp.coroutineLifetimeJob)
        )
    }
    DisposableEffect(screenContext) {
        onDispose { screenContext.lifetime.cancel() }
    }
    CompositionLocalProvider(
        LocalScreenContext provides screenContext,
        LocalComponentContext provides screenContext,
        content = content
    )
}