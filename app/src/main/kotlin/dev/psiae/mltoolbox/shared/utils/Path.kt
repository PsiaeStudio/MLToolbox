package dev.psiae.mltoolbox.shared.utils

import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

@ExperimentalPathApi
fun Path.deleteRecursivelyBool() =
    runCatching {
        deleteRecursively()
    }.fold(
        onSuccess = { true },
        onFailure = { e ->
            when (e) {
                is IOException -> false
                else -> throw e
            }
        }
    )