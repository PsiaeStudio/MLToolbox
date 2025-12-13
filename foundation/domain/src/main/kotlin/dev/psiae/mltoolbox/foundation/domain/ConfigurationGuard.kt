package dev.psiae.mltoolbox.foundation.domain

interface ConfigurationGuard {

    val name: String
    val reason: String

    val isEditing: Boolean
}

fun ConfigurationGuard(
    name: String,
    reason: String,
    isEditing: Boolean = false,
): ConfigurationGuard {

    return object : ConfigurationGuard {
        override val name: String
            get() = name
        override val reason: String
            get() = reason
        override val isEditing: Boolean
            get() = isEditing
    }
}