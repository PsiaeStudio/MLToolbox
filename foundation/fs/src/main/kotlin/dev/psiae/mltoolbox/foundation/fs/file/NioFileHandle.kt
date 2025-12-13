package dev.psiae.mltoolbox.foundation.fs.file

import java.nio.channels.FileChannel

class NioFileHandle(
    readWrite: Boolean,
    private val fileChannel: FileChannel,
) : FileHandle(readWrite) {


}