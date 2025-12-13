plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    api(project(":core"))
    api(project(":foundation:native"))
    api(libs.filekit.core)
    api(libs.okio)
    compileOnly(libs.okio) {
        artifact {
            classifier = "sources"
        }
    }
}