plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    api(project(":foundation"))
    api(project(":shared:data"))
    api(project(":shared:domain"))
    api(project(":shared:ui"))
    api(project(":shared:app"))
}