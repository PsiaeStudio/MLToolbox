plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.compose.compiler)
}

dependencies {
    api(project(":foundation:ui"))
}