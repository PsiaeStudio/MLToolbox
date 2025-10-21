package dev.psiae.mltoolbox.shared.ui.composeui

import androidx.compose.runtime.Stable

// TODO: use kotlinx immutable collection library instead ?
@Stable
class StableList <T> (val content: List<T>): List<T> by content {

    override fun equals(other: Any?): Boolean {
        return content.equals(other)
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    override fun toString(): String {
        return content.toString()
    }
}

fun <T> List<T>.wrapStableList() = StableList(this)