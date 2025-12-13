package dev.psiae.mltoolbox.foundation.fs.file

import java.io.RandomAccessFile

class JvmFileHandle(
    readWrite: Boolean,
    private val randomAccessFile: RandomAccessFile,
) : FileHandle(readWrite) {

}