package dev.psiae.mltoolbox.shared.libs

import dev.psiae.mltoolbox.core.LazyConstructor

object NativeLibsInitializer {
    private val INIT = LazyConstructor<Unit>()

    fun init() = INIT.constructOrThrow(
        lazyValue = ::doInit,
        lazyThrow = ::alreadyInitErr
    )

    private fun doInit() {
        initSevenZip()
    }

    private fun alreadyInitErr(): Nothing = error("NativeLibInitializer already init")

    private fun initSevenZip() {
        SevenZipNativeLibInitializer.init()
    }
}