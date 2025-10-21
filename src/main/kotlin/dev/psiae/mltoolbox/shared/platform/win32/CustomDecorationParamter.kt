package dev.psiae.mltoolbox.shared.platform.win32

import java.util.concurrent.atomic.AtomicInteger

// based on https://github.com/kalibetre/CustomDecoratedJFrame
// fixme: we will not be using singleton
object CustomDecorationParameters {

private var _titleBarHeight = AtomicInteger(45)

    private var _controlBoxWidth: AtomicInteger = AtomicInteger(150)

    private var _iconWidth: AtomicInteger = AtomicInteger(0)

    private var _extraLeftReservedWidth: AtomicInteger = AtomicInteger()

    private var _extraRightReservedWidth: AtomicInteger = AtomicInteger(0)

    private var _maximizedWindowFrameThickness: AtomicInteger = AtomicInteger(10)

    private var _frameResizeBorderThickness: AtomicInteger = AtomicInteger(4)

    private var _frameBorderThickness: AtomicInteger = AtomicInteger(1)

    var titleBarHeight
        get() = _titleBarHeight.get()
        set(value) { _titleBarHeight.set(value) }

    var controlBoxWidth
        get() = _controlBoxWidth.get()
        set(value) = _controlBoxWidth.set(value)

    var iconWidth
        get() = _iconWidth.get()
        set(value) = _iconWidth.set(value)

    var extraLeftReservedWidth
        get() = _extraLeftReservedWidth.get()
        set(value) = _extraLeftReservedWidth.set(value)

    var extraRightReservedWidth
        get() = _extraRightReservedWidth.get()
        set(value) = _extraRightReservedWidth.set(value)

    var maximizedWindowFrameThickness
        get() = _maximizedWindowFrameThickness.get()
        set(value) = _maximizedWindowFrameThickness.set(value)

    var frameResizeBorderThickness
        get() = _frameResizeBorderThickness.get()
        set(value) { _frameResizeBorderThickness.set(value) }

    var frameBorderThickness
        get() = _frameBorderThickness.get()
        set(value) { _frameResizeBorderThickness.set(value) }

    @Volatile
    var controlBoxRightOffset = 0

    @Volatile
    var controlBoxTopOffset = 0

    @Volatile
    var controlBoxHeight = 0
}