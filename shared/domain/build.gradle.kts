plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    api(project(":foundation"))
    api(libs.androidx.datastore.core)
}