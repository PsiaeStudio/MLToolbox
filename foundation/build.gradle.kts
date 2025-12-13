import org.gradle.kotlin.dsl.dependencies

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    api(project(":core"))
    api(project(":foundation:compress"))
    api(project(":foundation:db"))
    api(project(":foundation:fs"))
    api(project(":foundation:io"))
    api(project(":foundation:native"))
    api(project(":foundation:domain"))
    api(project(":foundation:ui"))
}