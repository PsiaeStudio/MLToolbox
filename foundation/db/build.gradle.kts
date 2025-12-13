plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    api(project(":core"))
    api(libs.sqldelight.driver)
}