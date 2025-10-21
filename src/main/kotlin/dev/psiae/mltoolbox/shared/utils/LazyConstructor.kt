package dev.psiae.mltoolbox.shared.utils

import kotlin.reflect.KProperty

class LazyConstructor<T> @JvmOverloads constructor(lock: Any = Any()) {

    /**
     * Placeholder Object
     */
    private object UNSET

    /**
     * The Lock
     */
    private val lock: Any = lock

    /**
     * The value holder. [UNSET] if not set
     *
     * @throws IllegalStateException if trying to set value without lock
     * @throws IllegalStateException if value was already set
     */
    @Volatile
    private var localValue: Any? = UNSET
        set(value) {
            check(Thread.holdsLock(lock)) {
                "Trying to set field without lock"
            }
            check(field === UNSET) {
                "localValue was $field when trying to set $value"
            }
            field = value
        }

    @Suppress("UNCHECKED_CAST")
    private val castValue: T
        get() = try {
            localValue as T
        } catch (cce: ClassCastException) {
            error("localValue=$localValue was UNSET")
        }

    /**
     * The value.
     *
     * @throws IllegalStateException if [localValue] is [UNSET]
     */
    val value: T
        get() {
            if (!isConstructed()) {
                // The value is not yet initialized, check if its still being initialized.
                // If not then IllegalStateException will be thrown
                sync()
            }
            return castValue
        }

    /**
     *  Whether [localValue] is already initialized
     *  @see isConstructedSync
     */
    fun isConstructed() = localValue !== UNSET

    /**
     * Whether [localValue] is already initialized, synchronized
     * @see isConstructed
     */
    fun isConstructedSync() = sync { isConstructed() }

    /** Construct the delegated value, if not already constructed */
    fun construct(lazyValue: () -> T): T {
        if (isConstructed()) {
            return castValue
        }
        return sync {
            if (!isConstructed()) {
                localValue = lazyValue()
            }
            castValue
        }
    }

    fun constructOrThrow(
        lazyValue: () -> T,
        lazyThrow: () -> Nothing
    ): T {
        if (isConstructed()) {
            lazyThrow()
        }
        return sync {
            if (!isConstructed()) {
                localValue = lazyValue()
            } else {
                lazyThrow()
            }
            castValue
        }
    }

    private fun sync(): Unit = sync { }
    private fun <T> sync(block: () -> T): T = synchronized(lock) { block() }
}

fun <T> LazyConstructor<T>.valueOrNull(): T? {
    return try { value } catch (ise: IllegalStateException) { null }
}

operator fun <T> LazyConstructor<T>.getValue(receiver: Any?, property: KProperty<*>): T {
    return value
}

operator fun <T> LazyConstructor<T>.setValue(receiver: Any?, property: KProperty<*>, value: T) {
    construct { value }
}

fun <T> LazyConstructor<T>.toLazy(): Lazy<T> = object : Lazy<T> {
    override val value: T get() = this@toLazy.value
    override fun isInitialized(): Boolean = this@toLazy.isConstructed()
}