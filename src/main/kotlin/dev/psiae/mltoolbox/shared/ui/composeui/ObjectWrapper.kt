package dev.psiae.mltoolbox.shared.ui.composeui

object NeverEqualObject {

    // this object is never equal
    override fun equals(other: Any?): Boolean {
        return false
    }

    // structurally this object is the same
    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun toString(): String {
        return buildString {
            append("NeverEqualObject@${System.identityHashCode(this)}")
        }
    }
}

class DelegatedObjectEqual<T, DELEGATE>(
    val obj: T,
    val delegate: DELEGATE
) {

    override fun equals(other: Any?): Boolean {
        return delegate == other
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun toString(): String {
        return buildString {
            append("DelegatedObjectEqual(obj=$obj, delegate=$delegate)")
        }
    }

    operator fun component1(): T {
        return obj
    }

    operator fun component2(): DELEGATE {
        return delegate
    }
}

class CustomObjectEqual<T>(
    val obj: T,
    val equality: (T, Any?) -> Boolean,
    val customHashCode: () -> Int = { obj.hashCode() }
) {

    override fun equals(other: Any?): Boolean {
        return equality.invoke(obj, other)
    }

    override fun hashCode(): Int {
        return customHashCode.invoke()
    }
}

fun <T> delegatedObjectNeverEqual(obj: T) = DelegatedObjectEqual(obj, NeverEqualObject)