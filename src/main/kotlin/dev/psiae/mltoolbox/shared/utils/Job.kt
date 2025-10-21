package dev.psiae.mltoolbox.shared.utils

import kotlinx.coroutines.Job

fun Job?.isNullOrNotActive() = this?.isActive != true