plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.compose.compiler)
}

dependencies {
    api(project(":core"))
    api(libs.jetbrains.compose.foundation)
    api(libs.jetbrains.compose.material)
    api(libs.jetbrains.compose.material3)
    api(libs.jetbrains.compose.desktop.jvm.windows.x64)

    // note: we don't use these
    compileOnly(libs.arkivanov.decompose)
    compileOnly(libs.androidx.navigation3.runtime)
    compileOnly(libs.androidx.navigation3.ui)
    compileOnly(libs.coil3.compose)
}