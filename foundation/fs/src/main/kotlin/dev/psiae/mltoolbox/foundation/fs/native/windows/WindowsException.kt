package dev.psiae.mltoolbox.foundation.fs.native.windows

internal class WindowsException : Exception {
    private val lastError: Int
    private var msg: String?

    constructor(lastError: Int) {
        this.lastError = lastError
        this.msg = null
    }

    constructor(msg: String?) {
        this.lastError = 0
        this.msg = msg
    }

    fun lastError(): Int {
        return lastError
    }
}
