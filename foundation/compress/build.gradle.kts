plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    api(project(":core"))
    api(libs.sevenzipjbinding)
    api(libs.sevenzipjbinding.windows.amd64)
    api(libs.junrar)
    api(libs.zip4j)
}