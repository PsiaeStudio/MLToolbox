package dev.psiae.mltoolbox.shared.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object UuidV7 {

    fun generate() = Uuid.generateV7();
}