package dev.psiae.mltoolbox.core.java

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

typealias jFile = File
typealias jPath = Path

fun jPath(path: String): jPath = Path(path)

fun String.toNioPath(): jPath = jPath(this)