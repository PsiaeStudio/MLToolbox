plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.compose.plugin)
}

dependencies {
    implementation(project(":foundation"))
    implementation(project(":shared"))
}