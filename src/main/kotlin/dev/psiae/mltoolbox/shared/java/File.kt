package dev.psiae.mltoolbox.shared.java

typealias jFile = java.io.File
typealias jPath = java.nio.file.Path

fun jPath(path: String): jPath = kotlin.io.path.Path(path)

fun String.toNioPath(): jPath = jPath(this)