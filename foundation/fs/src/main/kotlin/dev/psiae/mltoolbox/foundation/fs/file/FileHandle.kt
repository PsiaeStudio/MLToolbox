/*
 * Copyright (C) 2021 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.psiae.mltoolbox.foundation.fs.file

import okio.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * An open file for reading and writing; using either streaming and random access.
 *
 * Use [read] and [write] to perform one-off random-access reads and writes. Use [source], [sink],
 * and [appendingSink] for streaming reads and writes.
 *
 * File handles must be closed when they are no longer needed. It is an error to read, write, or
 * create streams after a file handle is closed. The operating system resources held by a file
 * handle will be released once the file handle **and** all of its streams are closed.
 *
 * Although this class offers both reading and writing APIs, file handle instances may be
 * read-only or write-only. For example, a handle to a file on a read-only file system will throw an
 * exception if a write is attempted.
 *
 * File handles may be used by multiple threads concurrently. But the individual sources and sinks
 * produced by a file handle are not safe for concurrent use.
 */
abstract class FileHandle(
    /**
     * True if this handle supports both reading and writing. If this is false all write operations
     * including [write], [sink], [resize], and [flush] will all throw [IllegalStateException] if
     * called.
     */
    val readWrite: Boolean,
) {
    /**
     * True once the file handle is closed. Resources should be released with [protectedClose] once
     * this is true and [openStreamCount] is 0.
     */
    private var closed = false

    /**
     * Reference count of the number of open sources and sinks on this file handle. Resources should
     * be released with [protectedClose] once this is 0 and [closed] is true.
     */
    private var openStreamCount = 0

    val lock: Lock = ReentrantLock()
}