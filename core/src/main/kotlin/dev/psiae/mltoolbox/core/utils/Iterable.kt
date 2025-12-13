package dev.psiae.mltoolbox.core.utils

inline fun <T, K> Iterable<T>.anyDuplicate(selector: (T) -> K): Boolean {
    val set = HashSet<K>()
    for (element in this) {
        val key = selector(element)
        if (!set.add(key)) {
            return true
        }
    }
    return false
}