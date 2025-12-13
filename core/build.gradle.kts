plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlinx.atomicfu)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.core.jvm)
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    api(libs.log4k)
}