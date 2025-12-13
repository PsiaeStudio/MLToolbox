package dev.psiae.mltoolbox.foundation.domain

import dev.psiae.mltoolbox.core.LazyConstructor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ConfigurationWarden internal constructor() {
    private val lock = ReentrantLock()
    private val configurationGuardMap = HashMap<ConfigurationKey, MutableSet<ConfigurationGuard>>()
    private val guardToConfigurationMap = HashMap<ConfigurationGuard, Set<ConfigurationKey>>()

    private val guardEvents = MutableSharedFlow<ConfigurationKey>()

    fun placeGuard(
        key: ConfigurationKey,
        guard: ConfigurationGuard
    ) {
        lock.withLock {
            val entry = configurationGuardMap[key]
            if (entry != null) {
                if (!entry.add(guard))
                    throw IllegalArgumentException("Configuration guard ${guard.name} already exists")
            } else {
                configurationGuardMap[key] = mutableSetOf<ConfigurationGuard>().apply { add(guard) }
            }
        }
        guardEvents.tryEmit(key)
    }

    fun placeGuardIf(
        keys: Set<ConfigurationKey>,
        predicate: (key: ConfigurationKey, guards: Set<ConfigurationGuard>) -> Boolean,
        getGuard: () -> ConfigurationGuard
    ): ConfigurationGuard? {
        if (keys.isEmpty())
            return null
        var guard: ConfigurationGuard? = null
        lock.withLock {
            keys.forEach { key ->
                val entry = configurationGuardMap[key]
                if (!predicate(key, entry ?: emptySet()))
                    return null
            }
            guard = getGuard()
            keys.forEach { key ->
                val existingGuards = configurationGuardMap[key]
                if (existingGuards != null && existingGuards.contains(guard))
                    throw IllegalArgumentException("Configuration guard ${guard.name} already exists")
            }
            keys.forEach { key ->
                val entry = configurationGuardMap.getOrPut(key) { mutableSetOf() }
                entry.add(guard)
            }
            guardToConfigurationMap[guard] = keys.toSet()
            keys.forEach { key ->
                guardEvents.tryEmit(key)
            }
        }
        return guard
    }


    fun removeGuard(
        guard: ConfigurationGuard
    ): Boolean {
        val removedKeys = mutableSetOf<ConfigurationKey>()
        lock.withLock {
            guardToConfigurationMap[guard]?.let { keys ->
                keys.forEach { key ->
                    if (configurationGuardMap[key]?.remove(guard) == true) {
                        removedKeys.add(key)
                    }
                }
                checkNotNull(guardToConfigurationMap.remove(guard))
            }
        }
        removedKeys.forEach { key ->
            guardEvents.tryEmit(key)
        }
        return removedKeys.isNotEmpty()
    }

    fun isGuarded(
        key: ConfigurationKey
    ): Boolean {
        return lock.withLock {
            configurationGuardMap[key].isNullOrEmpty()
        }
    }

    fun isAnyGuard(
        key: ConfigurationKey,
        predicate: (ConfigurationGuard) -> Boolean
    ): Boolean {
        return lock.withLock {
            configurationGuardMap[key]?.any(predicate) ?: false
        }
    }

    fun observeIsGuarded(key: ConfigurationKey): Flow<Boolean> {
        return guardEvents
            .filter { it == key }
            .onStart { emit(key) }
            .map { isGuarded(it) }
            .distinctUntilChanged()
    }

    companion object {
        private val INSTANCE = LazyConstructor<ConfigurationWarden>()

        fun getInstance(): ConfigurationWarden {
            return INSTANCE.construct { ConfigurationWarden() }
        }
    }
}