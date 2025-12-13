package dev.psiae.mltoolbox.core

object JvmProperties {
    val USER_DIR = requireSystemProperty("user.dir")
    val JAVA_IO_TMPDIR = requireSystemProperty("java.io.tmpdir")
    val OS_NAME = requireSystemProperty("os.name")
    val OS_ARCH = requireSystemProperty("os.arch")
    val OS_VERSION = requireSystemProperty("os.version")

    private fun requireSystemProperty(
        key: String
    ) = checkNotNull(System.getProperty(key)) {
        "System property is null: $key"
    }
}